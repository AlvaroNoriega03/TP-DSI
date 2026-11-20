package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo NavTrack QX-7A.
 * Usa el código de la CM DESTINO (no el de origen) y convierte el String separado por comas.
 */
@Component
public class AdaptadorGPSNavTrackQX7A implements IAdaptadorGPSTracker {

    public static final String MODELO = "NavTrack QX-7A";

    private final GPSTrackerNavTrackQX7A gpsTracker;
    private final String apiKey;

    public AdaptadorGPSNavTrackQX7A(GPSTrackerNavTrackQX7A gpsTracker,
                                    @Value("${gps.api-key.navtrack-qx-7a}") String apiKey) {
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
            String texto = gpsTracker.retrieveTrackingData(apiKey, numeroBolsin, codigoCMDestino);
            return convertirTextoSeparadoPorComas(texto);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** "101,-31.4201,-64.1888,2026-03-07T12:30" */
    private UbicacionBolsin convertirTextoSeparadoPorComas(String texto) throws UbicacionNoDisponibleException {
        try {
            String[] items = texto.trim().split(",");
            return new UbicacionBolsin(
                    Integer.parseInt(items[0].trim()),
                    Double.parseDouble(items[1].trim()),
                    Double.parseDouble(items[2].trim()),
                    LocalDateTime.parse(items[3].trim()));
        } catch (Exception e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida del NavTrack QX-7A: " + texto, e);
        }
    }
}
