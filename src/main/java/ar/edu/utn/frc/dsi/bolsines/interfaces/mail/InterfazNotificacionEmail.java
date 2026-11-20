package ar.edu.utn.frc.dsi.bolsines.interfaces.mail;

import ar.edu.utn.frc.dsi.bolsines.entidades.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.interfaces.gps.UbicacionBolsin;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.IObservadorBolsines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

/**
 * «boundary» hacia el Sistema de Correo Electrónico (RNF 5: API de Outlook / Microsoft Graph).
 * Representa la ejecución del CU 31 "Notificar ubicación de bolsín" (incluido por el CU 36).
 * PATRÓN OBSERVER - Observador concreto: el GestorUbicacionBolsines la crea (new), la suscribe y,
 * al notificar, actualizar(...) arma el correo y lo envía con enviarNotificacion(...).
 * En el prototipo el envío se SIMULA: el correo se escribe en la consola de la aplicación.
 */
public class InterfazNotificacionEmail implements IObservadorBolsines {

    private static final Logger log = LoggerFactory.getLogger(InterfazNotificacionEmail.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void actualizar(Bolsin bolsin, String emailGCM, UbicacionBolsin ubicacion) {
        String notificacion = "Para: " + emailGCM + " | Asunto: Ubicación del bolsín N° " + bolsin.getNumeroBolsin() + "\n"
                + "Se informa la ubicación del bolsín N° " + bolsin.getNumeroBolsin()
                + " con destino a " + bolsin.getCMDestino().getNombre() + ".\n"
                + "Última actualización: " + ubicacion.fechaHoraUltimaActualizacion().format(FORMATO) + "\n"
                + "Coordenadas: " + ubicacion.latitud() + ", " + ubicacion.longitud() + "\n"
                + "Ver en mapa: https://www.google.com/maps?q=" + ubicacion.latitud() + "," + ubicacion.longitud();
        enviarNotificacion(notificacion);
    }

    /** Mensaje al actor Sistema de Correo Electrónico (simulado). */
    public void enviarNotificacion(String notificacion) {
        log.info("[CU 31 - Sistema de Correo Electrónico simulado] Correo enviado:\n{}", notificacion);
    }
}
