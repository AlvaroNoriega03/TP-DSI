package ar.edu.utn.frc.dsi.bolsines.interfaces.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Boundary hacia el servidor de correo (RNF 5: API de Outlook / Microsoft Graph).
 * Representa la ejecución del CU 31 "Notificar ubicación de bolsín" (incluido por el CU 36).
 * En el prototipo el envío se SIMULA: se arma el correo, se escribe en el log y se guarda en memoria.
 */
@Component
public class InterfazNotificacionMail {

    private static final Logger log = LoggerFactory.getLogger(InterfazNotificacionMail.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final List<String> correosEnviados = Collections.synchronizedList(new ArrayList<>());

    public boolean notificarUbicacionBolsin(String emailGCMDestino, Integer numeroBolsin,
                                            LocalDateTime fechaHoraUltimaActualizacion,
                                            Double latitud, Double longitud) {
        String asunto = "Ubicación del bolsín N° " + numeroBolsin;
        String cuerpo = "Se informa la ubicación del bolsín N° " + numeroBolsin + ".\n"
                + "Última actualización: " + fechaHoraUltimaActualizacion.format(FORMATO) + "\n"
                + "Coordenadas: " + latitud + ", " + longitud + "\n"
                + "Ver en mapa: https://www.google.com/maps?q=" + latitud + "," + longitud;
        String correo = "Para: " + emailGCMDestino + " | Asunto: " + asunto + "\n" + cuerpo;
        correosEnviados.add(correo);
        log.info("[CU 31 - Microsoft Graph simulado] Correo enviado:\n{}", correo);
        return true;
    }

    public List<String> getCorreosEnviados() {
        return List.copyOf(correosEnviados);
    }
}
