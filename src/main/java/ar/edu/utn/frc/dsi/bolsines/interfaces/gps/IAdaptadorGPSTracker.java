package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

/**
 * PATRÓN ADAPTER - Target (Objetivo).
 * Interfaz única que conoce el GestorSeguimientoBolsines (Cliente) para obtener la ubicación
 * de un bolsín, sin importar qué modelo de GPS Tracker tenga colocado.
 */
public interface IAdaptadorGPSTracker {

    /** ¿Este adaptador sabe comunicarse con el modelo de GPS Tracker indicado? */
    boolean esDeModelo(String modeloGPSTracker);

    /**
     * Devuelve la ubicación del bolsín en un formato común (número, latitud, longitud y fecha/hora).
     * Recibe todos los datos que podría necesitar cualquier modelo; cada adaptador usa los que le sirven.
     *
     * @throws UbicacionNoDisponibleException si el GPS Tracker no pudo informar la ubicación (flujo A3).
     */
    UbicacionBolsin obtenerUbicacionBolsin(Integer numeroBolsin, String codigoCMOrigen, String codigoCMDestino)
            throws UbicacionNoDisponibleException;
}
