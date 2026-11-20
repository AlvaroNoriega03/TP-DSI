package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

/** Error de comunicación con la API de un GPS Tracker (respuesta HTTP distinta de 200, API caída, etc.). */
public class GPSTrackerException extends RuntimeException {

    private final int codigoHttp;

    public GPSTrackerException(String mensaje, int codigoHttp) {
        super(mensaje);
        this.codigoHttp = codigoHttp;
    }

    public GPSTrackerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoHttp = -1;
    }

    /** -1 si no hubo respuesta HTTP (por ejemplo, la API no está levantada). */
    public int getCodigoHttp() {
        return codigoHttp;
    }
}
