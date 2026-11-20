package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * PATRÓN ADAPTER - Adaptee (Adaptado) del modelo GeoPulse MTR-900.
 * fetchCargoPositions(apiKey, numeroBolsin entero) devuelve una MATRIZ: cada fila es
 * [numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion].
 */
@Component
public class GPSTrackerGeoPulseMTR900 {

    private static final String ENDPOINT = "/api/geopulse-mtr-900/fetchCargoPositions";

    private final String urlBase;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GPSTrackerGeoPulseMTR900(@Value("${gps.url-base}") String urlBase) {
        this.urlBase = urlBase;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public Object[][] fetchCargoPositions(String apiKey, int numeroBolsin) {
        String url = urlBase + ENDPOINT + "?numeroBolsin=" + numeroBolsin;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("X-API-Key", apiKey)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new GPSTrackerException("GeoPulse MTR-900 respondió " + response.statusCode() + ": " + response.body(),
                        response.statusCode());
            }
            List<List<Object>> filas = objectMapper.readValue(response.body(), new TypeReference<List<List<Object>>>() { });
            Object[][] matriz = new Object[filas.size()][];
            for (int i = 0; i < filas.size(); i++) {
                matriz[i] = filas.get(i).toArray();
            }
            return matriz;
        } catch (IOException e) {
            throw new GPSTrackerException("No se pudo conectar con el GPS Tracker GeoPulse MTR-900 (" + urlBase + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GPSTrackerException("Consulta al GPS Tracker GeoPulse MTR-900 interrumpida", e);
        }
    }
}
