package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

/** Flujo alternativo A3: el GPS Tracker no pudo informar la ubicación del bolsín. */
public class UbicacionNoDisponibleException extends Exception {

    public UbicacionNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public UbicacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
