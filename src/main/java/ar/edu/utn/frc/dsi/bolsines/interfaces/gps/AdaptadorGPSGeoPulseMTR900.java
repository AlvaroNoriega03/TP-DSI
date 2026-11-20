package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo GeoPulse MTR-900.
 * Sólo envía el número de bolsín (no usa códigos de CM) y convierte la matriz recibida.
 */
@Component
public class AdaptadorGPSGeoPulseMTR900 implements IAdaptadorGPSTracker {

    public static final String MODELO = "GeoPulse MTR-900";

    private final GPSTrackerGeoPulseMTR900 gpsTracker;
    private final String apiKey;

    public AdaptadorGPSGeoPulseMTR900(GPSTrackerGeoPulseMTR900 gpsTracker,
                                      @Value("${gps.api-key.geopulse-mtr-900}") String apiKey) {
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
            Object[][] matriz = gpsTracker.fetchCargoPositions(apiKey, numeroBolsin);
            return convertirMatriz(matriz, numeroBolsin);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** [[101, -31.4201, -64.1888, "2026-03-07T12:30"]] */
    private UbicacionBolsin convertirMatriz(Object[][] matriz, Integer numeroBolsin) throws UbicacionNoDisponibleException {
        try {
            for (Object[] fila : matriz) {
                int nro = ((Number) fila[0]).intValue();
                if (nro == numeroBolsin) {
                    return new UbicacionBolsin(
                            nro,
                            ((Number) fila[1]).doubleValue(),
                            ((Number) fila[2]).doubleValue(),
                            LocalDateTime.parse(String.valueOf(fila[3])));
                }
            }
        } catch (Exception e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida del GeoPulse MTR-900", e);
        }
        throw new UbicacionNoDisponibleException("GeoPulse MTR-900 no informó la ubicación del bolsín " + numeroBolsin);
    }
}
