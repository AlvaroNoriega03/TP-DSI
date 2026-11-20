package ar.edu.utn.frc.dsi.bolsines.gestores;

import ar.edu.utn.frc.dsi.bolsines.dto.DatosSeguimientoBolsin;
import ar.edu.utn.frc.dsi.bolsines.entidades.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entidades.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entidades.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidades.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entidades.Usuario;
import ar.edu.utn.frc.dsi.bolsines.interfaces.gps.ILocalizadorBolsines;
import ar.edu.utn.frc.dsi.bolsines.interfaces.gps.UbicacionBolsin;
import ar.edu.utn.frc.dsi.bolsines.interfaces.gps.UbicacionNoDisponibleException;
import ar.edu.utn.frc.dsi.bolsines.interfaces.mail.InterfazNotificacionEmail;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.IObservadorBolsines;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.IObservadorSeguimiento;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.ISujetoBolsines;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.ISujetoSeguimiento;
import ar.edu.utn.frc.dsi.bolsines.pantallas.PantallaUbicacionBolsines;
import ar.edu.utn.frc.dsi.bolsines.repositorios.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.sesion.ContextoSesion;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * «control» del CU 36 Consultar seguimiento de bolsines.
 * - PATRÓN ADAPTER: es el Cliente. Sólo conoce ILocalizadorBolsines, nunca a los GPS Trackers concretos.
 * - PATRÓN OBSERVER (Vista Dinámica - Observer): es el Sujeto concreto del envío del correo (ISujetoBolsines).
 *   Al confirmar el envío crea InterfazNotificacionEmail, la suscribe y la notifica con notificar().
 * - PATRÓN OBSERVER (listado y mapa): también es Sujeto de ISujetoSeguimiento. Con notificarSeguimiento()
 *   avisa a PantallaUbicacionBolsines e InterfazGoogleMaps cuando obtiene, actualiza o filtra las ubicaciones.
 */
@Component
@SessionScope
public class GestorUbicacionBolsines implements ISujetoSeguimiento, ISujetoBolsines {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Dependencias (persistencia e interfaces externas) ---
    private final ContextoSesion contextoSesion;
    private final SesionRepository sesionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final BolsinRepository bolsinRepository;
    private final List<ILocalizadorBolsines> localizadores;

    // --- Observer (listado y mapa) ---
    private final List<IObservadorSeguimiento> observadores = new ArrayList<>();

    // --- Observer (envío del correo al GCM) ---
    private final List<IObservadorBolsines> observadoresBolsines = new ArrayList<>();

    // --- Estado del caso de uso ---
    private PantallaUbicacionBolsines pantalla;
    private Empleado empleadoLogueado;
    private ComisionMedica cmEmpleadoLogueado;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private List<DatosSeguimientoBolsin> datosSeguimiento = new ArrayList<>();
    private List<DatosSeguimientoBolsin> datosVisibles = new ArrayList<>();
    private Bolsin bolsinSeleccionado;
    private DatosSeguimientoBolsin datosBolsinSeleccionado;
    private String emailGCM;
    private UbicacionBolsin ubicacionBolsinSeleccionado;

    public GestorUbicacionBolsines(ContextoSesion contextoSesion,
                                     SesionRepository sesionRepository,
                                     EmpleadoRepository empleadoRepository,
                                     BolsinRepository bolsinRepository,
                                     List<ILocalizadorBolsines> localizadores) {
        this.contextoSesion = contextoSesion;
        this.sesionRepository = sesionRepository;
        this.empleadoRepository = empleadoRepository;
        this.bolsinRepository = bolsinRepository;
        this.localizadores = localizadores;
    }

    // =====================================================================================
    // Flujo principal
    // =====================================================================================

    /** Pasos 2 a 6 del CU 36. */
    public void consultarSeguimientoBolsines(PantallaUbicacionBolsines pantalla) {
        this.pantalla = pantalla;
        limpiarEstado();

        // Paso 2: busca y muestra la CM del usuario logueado
        empleadoLogueado = buscarEmpleadoLogueado();
        if (empleadoLogueado == null) {
            pantalla.mostrarMensaje("error", "No se encontró el empleado del usuario logueado.");
            finCU();
            return;
        }
        cmEmpleadoLogueado = buscarCMEmpleadoLogueado();
        pantalla.mostrarCM(cmEmpleadoLogueado.getNombre());

        // Paso 3: busca los bolsines en estado Enviado cuya CM origen es la del usuario logueado
        bolsinesEnviados = buscarBolsinesEnviados();
        if (bolsinesEnviados.isEmpty()) {
            // A1: No se encuentran bolsines en estado enviado
            pantalla.mostrarMensaje("advertencia",
                    "No se encontraron bolsines en estado Enviado para " + cmEmpleadoLogueado.getNombre() + ".");
            pantalla.informarSinBolsines();
            finCU();
            return;
        }

        // Pasos 4 y 5: solicita y recibe la ubicación de cada bolsín (Adapter)
        datosSeguimiento = obtenerLocalizacionBolsines();
        datosVisibles = new ArrayList<>(datosSeguimiento);

        // Paso 6: muestra en el mapa la posición de los bolsines (Observer)
        notificarSeguimiento();
        pantalla.solicitarSeleccionBolsin();
    }

    /** Paso 2: el gestor busca la sesión actual y le pide el usuario; luego busca el empleado de ese usuario. */
    private Empleado buscarEmpleadoLogueado() {
        if (!contextoSesion.hayUsuarioLogueado()) {
            return null;
        }
        Sesion sesionActual = sesionRepository.findById(contextoSesion.getIdSesion()).orElse(null);
        if (sesionActual == null) {
            return null;
        }
        Usuario usuarioLogueado = sesionActual.getUsuario();
        for (Empleado empleado : empleadoRepository.findAll()) {
            if (empleado.esTuUsuario(usuarioLogueado)) {
                return empleado;
            }
        }
        return null;
    }

    private ComisionMedica buscarCMEmpleadoLogueado() {
        return empleadoLogueado.getCM();
    }

    /** Paso 3. */
    private List<Bolsin> buscarBolsinesEnviados() {
        List<Bolsin> encontrados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepository.findAll()) {
            if (bolsin.esTuCMOrigen(cmEmpleadoLogueado) && bolsin.sosEnviado()) {
                encontrados.add(bolsin);
            }
        }
        return encontrados;
    }

    /** Pasos 4 y 5: PATRÓN ADAPTER. El gestor le habla a todos los modelos de GPS de la misma forma. */
    private List<DatosSeguimientoBolsin> obtenerLocalizacionBolsines() {
        List<DatosSeguimientoBolsin> resultado = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            ILocalizadorBolsines localizador = buscarLocalizador(bolsin.getModeloGPSTracker());
            if (localizador == null) {
                resultado.add(crearDatosSinUbicacion(bolsin,
                        "No hay localizador para el modelo de GPS Tracker " + bolsin.getModeloGPSTracker()));
                continue;
            }
            try {
                UbicacionBolsin ubicacion = localizador.localizarBolsin(
                        bolsin.getNumeroBolsin(),
                        bolsin.getCMOrigen().getCodigo(),
                        bolsin.getCMDestino().getCodigo());
                resultado.add(crearDatosConUbicacion(bolsin, ubicacion));
            } catch (UbicacionNoDisponibleException e) {
                // A3: El GPS Tracker no pudo informar ubicación del bolsín
                resultado.add(crearDatosSinUbicacion(bolsin, e.getMessage()));
            }
        }
        return resultado;
    }

    private ILocalizadorBolsines buscarLocalizador(String modeloGPSTracker) {
        for (ILocalizadorBolsines localizador : localizadores) {
            if (localizador.esDeModelo(modeloGPSTracker)) {
                return localizador;
            }
        }
        return null;
    }

    /** El EB pide refrescar las posiciones (botón "Actualizar" o refresco automático). */
    public void actualizarUbicaciones() {
        if (bolsinesEnviados.isEmpty()) {
            return;
        }
        datosSeguimiento = obtenerLocalizacionBolsines();
        datosVisibles = new ArrayList<>(datosSeguimiento);
        notificarSeguimiento();
        pantalla.mostrarMensaje("info", "Ubicaciones actualizadas a las " + LocalDateTime.now().format(FORMATO_FECHA_HORA) + ".");
    }

    /** Paso 6: filtro por número de precinto. A2 si no se encuentra. */
    public void tomarFiltroPrecinto(Integer numeroPrecinto) {
        if (numeroPrecinto == null) {
            datosVisibles = new ArrayList<>(datosSeguimiento);
            notificarSeguimiento();
            return;
        }
        List<DatosSeguimientoBolsin> filtrados = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.tenesPrecinto(numeroPrecinto)) {
                filtrados.add(buscarDatosSeguimiento(bolsin.getNumeroBolsin()));
            }
        }
        if (filtrados.isEmpty()) {
            // A2: No se encuentra bolsín con el número de precinto ingresado
            pantalla.mostrarMensaje("advertencia",
                    "No se encontró un bolsín enviado con el número de precinto " + numeroPrecinto + ".");
            return;
        }
        datosVisibles = filtrados;
        notificarSeguimiento();
    }

    /** Paso 6: filtro por CM destino (código vacío = todas). */
    public void tomarFiltroCMDestino(String codigoCMDestino) {
        if (codigoCMDestino == null || codigoCMDestino.isBlank()) {
            datosVisibles = new ArrayList<>(datosSeguimiento);
            notificarSeguimiento();
            return;
        }
        List<DatosSeguimientoBolsin> filtrados = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.tenesCMDestino(codigoCMDestino)) {
                filtrados.add(buscarDatosSeguimiento(bolsin.getNumeroBolsin()));
            }
        }
        datosVisibles = filtrados;
        notificarSeguimiento();
    }

    /** Pasos 7 y 8: el EB selecciona un bolsín del mapa. */
    public void tomarSeleccionBolsin(Integer numeroBolsin) {
        bolsinSeleccionado = null;
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNumeroBolsin().equals(numeroBolsin)) {
                bolsinSeleccionado = bolsin;
            }
        }
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("error", "El bolsín " + numeroBolsin + " no está entre los bolsines en seguimiento.");
            return;
        }
        datosBolsinSeleccionado = buscarDatosSeguimiento(numeroBolsin);
        if (!datosBolsinSeleccionado.ubicado()) {
            pantalla.mostrarMensaje("advertencia",
                    "No se puede informar la ubicación del bolsín " + numeroBolsin + ": el GPS Tracker no la reportó.");
            return;
        }
        // Paso 8: consulta si se desea enviar el correo al GCM destino
        pantalla.solicitarConfirmacionEnvioCorreo(datosBolsinSeleccionado);
    }

    /** Pasos 9 a 11 (o A5 si no confirma). Ver "Vista Dinámica - Observer". */
    public void tomarConfirmacionEnvioCorreo(boolean confirma) {
        if (bolsinSeleccionado == null) {
            pantalla.mostrarMensaje("error", "Primero seleccione un bolsín del mapa.");
            return;
        }
        if (!confirma) {
            // A5: El EB no selecciona la opción para enviar un correo electrónico
            pantalla.mostrarMensaje("info", "No se envió el correo. Fin de la consulta.");
            finCU();
            return;
        }
        // Paso 10: busca la dirección de correo del GCM de la CM destino del bolsín
        emailGCM = buscarEmail();
        if (emailGCM == null) {
            pantalla.mostrarMensaje("error",
                    "No hay un Gerente de Comisión Médica registrado para " + bolsinSeleccionado.getCMDestino().getNombre() + ".");
            return;
        }
        // Paso 11: <<include>> CU 31 Notificar ubicación de bolsín
        llamarCasoDeUso31NotificarUbicacionBolsines(emailGCM);
        pantalla.informarNotificacionExitosa(bolsinSeleccionado.getNumeroBolsin(), emailGCM);
        finCU();
    }

    /**
     * Paso 10: loop buscar GCM [mientras haya empleados] de la CM destino del bolsín.
     * Se recorren sólo los empleados de la CM destino, por eso alcanza con preguntar sosGCM().
     */
    private String buscarEmail() {
        ComisionMedica cmDestino = bolsinSeleccionado.getCMDestino();
        for (Empleado empleado : empleadoRepository.findByComisionMedica(cmDestino)) {
            if (empleado.sosGCM()) {
                return empleado.getEmail();
            }
        }
        return null;
    }

    /** Paso 11: <<include>> CU 31. Se resuelve con el patrón Observer. */
    private void llamarCasoDeUso31NotificarUbicacionBolsines(String emailGCM) {
        this.emailGCM = emailGCM;
        this.ubicacionBolsinSeleccionado = new UbicacionBolsin(
                datosBolsinSeleccionado.numeroBolsin(),
                datosBolsinSeleccionado.latitud(),
                datosBolsinSeleccionado.longitud(),
                LocalDateTime.parse(datosBolsinSeleccionado.fechaHoraUltimaActualizacion(), FORMATO_FECHA_HORA));
        suscribirObservadores();
        notificar();
    }

    /** Crea los observadores del envío (new InterfazNotificacionEmail) y los suscribe. */
    private void suscribirObservadores() {
        InterfazNotificacionEmail interfazNotificacionEmail = new InterfazNotificacionEmail();
        suscribir(new IObservadorBolsines[]{interfazNotificacionEmail});
    }

    /** A4: el EB cancela la ejecución del caso de uso. */
    public void cancelarCU() {
        if (pantalla != null) {
            pantalla.mostrarMensaje("info", "Consulta cancelada.");
        }
        finCU();
    }

    private void finCU() {
        new ArrayList<>(observadores).forEach(this::quitar);
        new ArrayList<>(observadoresBolsines).forEach(this::quitar);
        if (pantalla != null) {
            pantalla.cerrarConsulta();
        }
        limpiarEstado();
    }

    // =====================================================================================
    // PATRÓN OBSERVER - Sujeto del listado y el mapa
    // =====================================================================================

    @Override
    public void suscribir(IObservadorSeguimiento observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void quitar(IObservadorSeguimiento observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarSeguimiento() {
        List<DatosSeguimientoBolsin> copia = List.copyOf(datosVisibles);
        for (IObservadorSeguimiento observador : observadores) {
            observador.actualizar(copia);
        }
    }

    // =====================================================================================
    // PATRÓN OBSERVER - Sujeto del envío del correo (Vista Dinámica - Observer)
    // =====================================================================================

    @Override
    public void suscribir(IObservadorBolsines[] observadores) {
        for (IObservadorBolsines observador : observadores) {
            if (!observadoresBolsines.contains(observador)) {
                observadoresBolsines.add(observador);
            }
        }
    }

    @Override
    public void quitar(IObservadorBolsines observador) {
        observadoresBolsines.remove(observador);
    }

    @Override
    public void notificar() {
        for (IObservadorBolsines observador : observadoresBolsines) {
            observador.actualizar(bolsinSeleccionado, emailGCM, ubicacionBolsinSeleccionado);
        }
    }

    // =====================================================================================
    // Auxiliares
    // =====================================================================================

    private DatosSeguimientoBolsin buscarDatosSeguimiento(Integer numeroBolsin) {
        for (DatosSeguimientoBolsin datos : datosSeguimiento) {
            if (datos.numeroBolsin().equals(numeroBolsin)) {
                return datos;
            }
        }
        return null;
    }

    private DatosSeguimientoBolsin crearDatosConUbicacion(Bolsin bolsin, UbicacionBolsin ubicacion) {
        return new DatosSeguimientoBolsin(
                bolsin.getNumeroBolsin(),
                bolsin.getNumeroPrecinto(),
                bolsin.getCMDestino().getCodigo(),
                bolsin.getCMDestino().getNombre(),
                bolsin.getModeloGPSTracker(),
                true,
                ubicacion.latitud(),
                ubicacion.longitud(),
                ubicacion.fechaHoraUltimaActualizacion().format(FORMATO_FECHA_HORA),
                null);
    }

    private DatosSeguimientoBolsin crearDatosSinUbicacion(Bolsin bolsin, String motivo) {
        return new DatosSeguimientoBolsin(
                bolsin.getNumeroBolsin(),
                bolsin.getNumeroPrecinto(),
                bolsin.getCMDestino().getCodigo(),
                bolsin.getCMDestino().getNombre(),
                bolsin.getModeloGPSTracker(),
                false,
                null,
                null,
                null,
                motivo);
    }

    private void limpiarEstado() {
        empleadoLogueado = null;
        cmEmpleadoLogueado = null;
        bolsinesEnviados = new ArrayList<>();
        datosSeguimiento = new ArrayList<>();
        datosVisibles = new ArrayList<>();
        bolsinSeleccionado = null;
        datosBolsinSeleccionado = null;
        emailGCM = null;
        ubicacionBolsinSeleccionado = null;
    }
}
