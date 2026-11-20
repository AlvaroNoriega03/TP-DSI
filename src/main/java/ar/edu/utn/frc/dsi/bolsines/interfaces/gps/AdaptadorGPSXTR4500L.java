package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo XTR-4500L (adaptador de objetos: compone al Adaptee).
 * Traduce obtenerUbicacionBolsin(...) a getBolsinLocation(apiKey, nro, codigoCMOrigen) y convierte el JSON
 * recibido al formato común UbicacionBolsin.
 */
@Component
public class AdaptadorGPSXTR4500L implements IAdaptadorGPSTracker {

    public static final String MODELO = "XTR-4500L";

    private final GPSTrackerXTR4500L gpsTracker;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdaptadorGPSXTR4500L(GPSTrackerXTR4500L gpsTracker,
                                @Value("${gps.api-key.xtr-4500l}") String apiKey) {
        this.gpsTracker = gpsTracker;
        this.apiKey = apiKey;
    }

    @Override
    public boolean esDeModelo(String modeloGPSTracker) {
        return MODELO.equalsIgnoreCase(modeloGPSTracker);
    }

    @Override
    public UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino)
            throws UbicacionNoDisponibleException {
        try {
            String json = gpsTracker.getBolsinLocation(apiKey, numeroBolsin, codigoCMOrigen);
            return convertirJSON(json, numeroBolsin);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** {"items":[{"numeroBolsin":101,"latitud":-31.42,"longitud":-64.18,"fechaHoraUltimaActualizacion":"..."}]} */
    private UbicacionBolsin convertirJSON(String json, Integer numeroBolsin) throws UbicacionNoDisponibleException {
        try {
            JsonNode items = objectMapper.readTree(json).path("items");
            for (JsonNode item : items) {
                if (item.path("numeroBolsin").asInt() == numeroBolsin) {
                    return new UbicacionBolsin(
                            item.path("numeroBolsin").asInt(),
                            item.path("latitud").asDouble(),
                            item.path("longitud").asDouble(),
                            LocalDateTime.parse(item.path("fechaHoraUltimaActualizacion").asText()));
                }
            }
            throw new UbicacionNoDisponibleException("XTR-4500L no informó la ubicación del bolsín " + numeroBolsin);
        } catch (UbicacionNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida del XTR-4500L: " + json, e);
        }
    }
}
