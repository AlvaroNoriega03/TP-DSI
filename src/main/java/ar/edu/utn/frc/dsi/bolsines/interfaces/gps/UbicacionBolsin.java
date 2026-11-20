package ar.edu.utn.frc.dsi.bolsines.interfaces.gps;

import java.time.LocalDateTime;

/**
 * Formato común de ubicación que devuelven todos los adaptadores (lo que pide la Observación 1 del CU 36:
 * número de bolsín, ubicación actual -latitud y longitud- y fecha y hora de la última actualización).
 */
public record UbicacionBolsin(Integer numeroBolsin,
                              Double latitud,
                              Double longitud,
                              LocalDateTime fechaHoraUltimaActualizacion) {
}
