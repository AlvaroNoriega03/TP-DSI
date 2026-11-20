package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER - Adapter concreto para el modelo NavTrack QX-7A.
 * Usa el código de la CM DESTINO (no el de origen) y convierte el String separado por comas.
 */
@Component
public class LocalizadorNavTrackQX7A implements ILocalizadorBolsines {

    public static final String MODELO = "NavTrack QX-7A";

    private final ServicioNavTrackQX7A servicio;
    private final String claveAPI;

    public LocalizadorNavTrackQX7A(ServicioNavTrackQX7A servicio,
                                    @Value("${gps.api-key.navtrack-qx-7a}") String claveAPI) {
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
            String texto = servicio.retrieveTrackingData(claveAPI, nroBolsin, codigoCMDestino);
            return convertirRespuestaLocalizacion(texto);
        } catch (GPSTrackerException e) {
            throw new UbicacionNoDisponibleException(e.getMessage(), e);
        }
    }

    /** "101,-31.4201,-64.1888,2026-03-07T12:30" */
    private UbicacionBolsin convertirRespuestaLocalizacion(String texto) throws UbicacionNoDisponibleException {
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
