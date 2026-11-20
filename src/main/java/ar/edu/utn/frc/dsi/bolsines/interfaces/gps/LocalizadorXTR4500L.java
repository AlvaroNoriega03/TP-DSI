package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo XTR-4500L (adaptador de objetos: compone al Adaptee).
 * Traduce localizarBolsin(...) a getBolsinLocation(claveAPI, nro, codigoCMOrigen) y convierte el JSON
 * recibido al formato común UbicacionBolsin.
 */
@Component
public class LocalizadorXTR4500L implements ILocalizadorBolsines {

    public static final String MODELO = "XTR-4500L";

    private final ServicioXTR4500L servicio;
    private final String claveAPI;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocalizadorXTR4500L(ServicioXTR4500L servicio,
                                @Value("${gps.api-key.xtr-4500l}") String claveAPI) {
        this.servicio = servicio;
        this.claveAPI = claveAPI;
    }

    @Override
    public boolean esDeModelo(String modeloGPS) {
        return MODELO.equalsIgnoreCase(modeloGPS);
    }

    @Override
    public UbicacionBolsin localizarBolsin(Integer nroBolsin, String codigoCMOrigen, String codigoCMDestino)
            throws UbicacionNoDisponibleException {
        try {
            String json = servicio.getBolsinLocation(claveAPI, nroBolsin, codigoCMOrigen);
            return convertirRespuestaLocalizacion(json, nroBolsin);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** {"items":[{"numeroBolsin":101,"latitud":-31.42,"longitud":-64.18,"fechaHoraUltimaActualizacion":"..."}]} */
    private UbicacionBolsin convertirRespuestaLocalizacion(String json, Integer nroBolsin) throws UbicacionNoDisponibleException {
        try {
            JsonNode items = objectMapper.readTree(json).path("items");
            for (JsonNode item : items) {
                if (item.path("numeroBolsin").asInt() == nroBolsin) {
                    return new UbicacionBolsin(
                            item.path("numeroBolsin").asInt(),
                            item.path("latitud").asDouble(),
                            item.path("longitud").asDouble(),
                            LocalDateTime.parse(item.path("fechaHoraUltimaActualizacion").asText()));
                }
            }
            throw new UbicacionNoDisponibleException("XTR-4500L no informó la ubicación del bolsín " + nroBolsin);
        } catch (UbicacionNoDisponibleException e) {
            throw e;
        } catch (Exception e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida del XTR-4500L: " + json, e);
        }
    }
}
