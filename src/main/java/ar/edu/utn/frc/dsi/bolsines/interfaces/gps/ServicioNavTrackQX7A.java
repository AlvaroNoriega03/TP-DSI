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
 * PATRÓN ADAPTER - Adaptee (Adaptado) del modelo NavTrack QX-7A.
 * retrieveTrackingData(claveAPI, bolsinID entero, código CM destino String) devuelve un único String
 * con los ítems separados por comas: "numeroBolsin,latitud,longitud,fechaHoraUltimoReporte".
 */
@Component
public class ServicioNavTrackQX7A {

    private static final String ENDPOINT = "/api/navtrack-qx-7a/retrieveTrackingData";

    private final String urlBase;
    private final HttpClient httpClient;

    public ServicioNavTrackQX7A(@Value("${gps.url-base}") String urlBase) {
        this.urlBase = urlBase;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public String retrieveTrackingData(String claveAPI, int bolsinID, String codCMDestino) {
        String url = urlBase + ENDPOINT
                + "?numeroBolsin=" + bolsinID
                + "&codigoComisionMedicaDestino=" + URLEncoder.encode(codCMDestino, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("X-API-Key", claveAPI)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GPSTrackerException("NavTrack QX-7A respondió " + response.statusCode() + ": " + response.body(),
                        response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            throw new GPSTrackerException("No se pudo conectar con el GPS Tracker NavTrack QX-7A (" + urlBase + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GPSTrackerException("Consulta al GPS Tracker NavTrack QX-7A interrumpida", e);
        }
    }
}
