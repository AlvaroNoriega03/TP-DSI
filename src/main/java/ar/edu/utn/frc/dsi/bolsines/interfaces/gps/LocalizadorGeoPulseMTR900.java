package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo GeoPulse MTR-900.
 * Sólo envía el número de bolsín (no usa códigos de CM) y convierte la matriz recibida.
 */
@Component
public class LocalizadorGeoPulseMTR900 implements ILocalizadorBolsines {

    public static final String MODELO = "GeoPulse MTR-900";

    private final ServicioGeoPulseMTR900 servicio;
    private final String claveAPI;

    public LocalizadorGeoPulseMTR900(ServicioGeoPulseMTR900 servicio,
                                      @Value("${gps.api-key.geopulse-mtr-900}") String claveAPI) {
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
            Object[][] matriz = servicio.fetchCargoPositions(claveAPI, nroBolsin);
            return convertirRespuestaLocalizacion(matriz, nroBolsin);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** [[101, -31.4201, -64.1888, "2026-03-07T12:30"]] */
    private UbicacionBolsin convertirRespuestaLocalizacion(Object[][] matriz, Integer nroBolsin) throws UbicacionNoDisponibleException {
        try {
            for (Object[] fila : matriz) {
                int nro = ((Number) fila[0]).intValue();
                if (nro == nroBolsin) {
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
        throw new UbicacionNoDisponibleException("GeoPulse MTR-900 no informó la ubicación del bolsín " + nroBolsin);
    }
}
