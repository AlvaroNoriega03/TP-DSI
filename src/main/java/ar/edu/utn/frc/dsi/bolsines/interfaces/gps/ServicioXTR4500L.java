package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * PATRÓN ADAPTER - Adaptee (Adaptado) del modelo XTR-4500L.
 * Representa al dispositivo/servicio externo tal como lo describe la Observación 1 del CU 36:
 * getBolsinLocation(claveAPI, bolsinID entero, código CM origen String) devuelve un JSON como String.
 * Su interfaz NO es compatible con la que espera el gestor: por eso existe LocalizadorXTR4500L.
 */
@Component
public class ServicioXTR4500L {

    private static final String ENDPOINT = "/api/xtr-4500l/getBolsinLocation";

    private final String urlBase;
    private final HttpClient httpClient;

    public ServicioXTR4500L(@Value("${gps.url-base}") String urlBase) {
        this.urlBase = urlBase;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    /** Endpoint del fabricante. Devuelve {"items":[{numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion}]}. */
    public String getBolsinLocation(String claveAPI, int bolsinID, String codCMOrigen) {
        String url = urlBase + ENDPOINT
                + "?numeroBolsin=" + bolsinID
                + "&codigoComisionMedicaOrigen=" + URLEncoder.encode(codCMOrigen, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("X-API-Key", claveAPI)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GPSTrackerException("XTR-4500L respondió " + response.statusCode() + ": " + response.body(),
                        response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new GPSTrackerException("No se pudo conectar con el GPS Tracker XTR-4500L (" + urlBase + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GPSTrackerException("Consulta al GPS Tracker XTR-4500L interrumpida", e);
        }
    }
}
