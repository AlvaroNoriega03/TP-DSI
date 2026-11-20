package ar.edu.utn.frc.dsi.bolsines.dto;

/**
 * Datos de un bolsín en seguimiento que el gestor envía a los observadores.
 * Si ubicado == false el GPS Tracker no pudo informar la posición (flujo A3) y motivoSinUbicacion explica por qué.
 */
public record DatosSeguimientoBolsin(Integer numeroBolsin,
                                     Integer numeroPrecinto,
                                     String codigoCMDestino,
                                     String nombreCMDestino,
                                     String modeloGPSTracker,
                                     boolean ubicado,
                                     Double latitud,
                                     Double longitud,
                                     String fechaHoraUltimaActualizacion,
                                     String motivoSinUbicacion) {
}
