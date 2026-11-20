package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

/**
 * PATRÓN ADAPTER - Target (Objetivo): ILocalizadorBolsines.
 * Interfaz única que conoce el GestorUbicacionBolsines (Cliente) para obtener la ubicación
 * de un bolsín, sin importar qué modelo de GPS Tracker tenga colocado.
 */
public interface ILocalizadorBolsines {

    /** ¿Este adaptador sabe comunicarse con el modelo de GPS Tracker indicado? */
    boolean esDeModelo(String modeloGPS);

    /**
     * Devuelve la ubicación del bolsín en un formato común (número, latitud, longitud y fecha/hora).
     * Recibe todos los datos que podría necesitar cualquier modelo; cada adaptador usa los que le sirven.
     *
     * @throws UbicacionNoDisponibleException si el GPS Tracker no pudo informar la ubicación (flujo A3).
     */
    UbicacionBolsin localizarBolsin(Integer nroBolsin, String codigoCMOrigen, String codigoCMDestino)
            throws UbicacionNoDisponibleException;
}
