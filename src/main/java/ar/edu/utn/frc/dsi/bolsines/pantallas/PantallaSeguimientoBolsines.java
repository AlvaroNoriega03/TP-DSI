package ar.edu.utn.frc.dsi.bolsines.pantallas;

import ar.edu.utn.frc.dsi.bolsines.dto.DatosSeguimientoBolsin;
import ar.edu.utn.frc.dsi.bolsines.dto.MensajePantalla;
import ar.edu.utn.frc.dsi.bolsines.gestores.GestorSeguimientoBolsines;
import ar.edu.utn.frc.dsi.bolsines.interfaces.mapa.InterfazGoogleMaps;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.IObservadorSeguimiento;
import ar.edu.utn.frc.dsi.bolsines.sesion.ContextoSesion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * «boundary» PantallaSeguimientoBolsines del CU 36.
 * - Los métodos "opc..." y "tomar..." reciben las acciones del EB (llegan desde el navegador por HTTP).
 * - Los métodos "mostrar...", "solicitar..." e "informar..." son los mensajes que el gestor le envía a la pantalla.
 * - PATRÓN OBSERVER - Observador concreto: actualizar(...) refresca el listado de bolsines y el filtro de CM destino.
 */
@RestController
@RequestMapping("/api/pantalla-seguimiento")
@SessionScope
public class PantallaSeguimientoBolsines implements IObservadorSeguimiento {

    private final GestorSeguimientoBolsines gestor;
    private final InterfazGoogleMaps interfazGoogleMaps;
    private final CanalEventosPantalla canal;
    private final ContextoSesion contextoSesion;

    public PantallaSeguimientoBolsines(GestorSeguimientoBolsines gestor,
                                       InterfazGoogleMaps interfazGoogleMaps,
                                       CanalEventosPantalla canal,
                                       ContextoSesion contextoSesion) {
        this.gestor = gestor;
        this.interfazGoogleMaps = interfazGoogleMaps;
        this.canal = canal;
        this.contextoSesion = contextoSesion;
    }

    // =====================================================================================
    // Canal de eventos hacia el navegador
    // =====================================================================================

    @GetMapping("/eventos")
    public SseEmitter conectarEventos() {
        return canal.conectar();
    }

    // =====================================================================================
    // Acciones del EB (entradas)
    // =====================================================================================

    /** Paso 1: el EB selecciona la opción para consultar la ubicación de uno o varios bolsines. */
    @PostMapping("/consultar")
    public ResponseEntity<Void> opcConsultarSeguimientoBolsines() {
        if (!contextoSesion.hayUsuarioLogueado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        habilitarPantalla();
        // Observer: la pantalla registra a los observadores en el sujeto
        gestor.suscribir(this);
        gestor.suscribir(interfazGoogleMaps);
        gestor.consultarSeguimientoBolsines(this);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/actualizar")
    public ResponseEntity<Void> tomarActualizacionUbicaciones() {
        gestor.actualizarUbicaciones();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/filtrar-precinto")
    public ResponseEntity<Void> tomarFiltroPrecinto(@RequestParam(required = false) Integer numeroPrecinto) {
        gestor.tomarFiltroPrecinto(numeroPrecinto);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/filtrar-cm-destino")
    public ResponseEntity<Void> tomarFiltroCMDestino(@RequestParam(required = false) String codigoCMDestino) {
        gestor.tomarFiltroCMDestino(codigoCMDestino);
        return ResponseEntity.accepted().build();
    }

    /** Paso 7: el EB selecciona un bolsín del mapa. */
    @PostMapping("/seleccionar")
    public ResponseEntity<Void> tomarSeleccionBolsin(@RequestParam Integer numeroBolsin) {
        gestor.tomarSeleccionBolsin(numeroBolsin);
        return ResponseEntity.accepted().build();
    }

    /** Paso 9 (confirma=true) o A5 (confirma=false). */
    @PostMapping("/confirmar-envio")
    public ResponseEntity<Void> tomarConfirmacionEnvioCorreo(@RequestParam boolean confirma) {
        gestor.tomarConfirmacionEnvioCorreo(confirma);
        return ResponseEntity.accepted().build();
    }

    /** A4: el EB cancela la ejecución del caso de uso. */
    @PostMapping("/cancelar")
    public ResponseEntity<Void> tomarCancelacion() {
        gestor.cancelarCU();
        return ResponseEntity.accepted().build();
    }

    // =====================================================================================
    // Mensajes del gestor a la pantalla (salidas)
    // =====================================================================================

    public void habilitarPantalla() {
        canal.enviar("pantalla", Map.of("estado", "habilitada"));
    }

    public void mostrarCM(String nombreCM) {
        canal.enviar("cm", Map.of("nombre", nombreCM));
    }

    public void solicitarSeleccionBolsin() {
        canal.enviar("solicitarSeleccion", Map.of("texto",
                "Seleccione un bolsín del mapa o filtre por número de precinto o CM destino."));
    }

    public void solicitarConfirmacionEnvioCorreo(DatosSeguimientoBolsin datos) {
        canal.enviar("confirmacion", datos);
    }

    public void informarNotificacionExitosa(Integer numeroBolsin, String emailGCM) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("numeroBolsin", numeroBolsin);
        datos.put("email", emailGCM);
        canal.enviar("notificacionExitosa", datos);
    }

    public void informarSinBolsines() {
        canal.enviar("sinBolsines", Map.of("estado", "vacio"));
    }

    public void mostrarMensaje(String tipo, String texto) {
        canal.enviar("mensaje", new MensajePantalla(tipo, texto));
    }

    public void cerrarConsulta() {
        canal.enviar("finCU", Map.of("estado", "finalizado"));
    }

    // =====================================================================================
    // PATRÓN OBSERVER - Observador
    // =====================================================================================

    @Override
    public void actualizar(List<DatosSeguimientoBolsin> datosSeguimiento) {
        canal.enviar("bolsines", datosSeguimiento);
    }
}
