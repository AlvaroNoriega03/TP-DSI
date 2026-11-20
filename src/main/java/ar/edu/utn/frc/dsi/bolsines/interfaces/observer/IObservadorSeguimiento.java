package ar.edu.utn.frc.dsi.bolsines.interfaces.observer;

import ar.edu.utn.frc.dsi.bolsines.dto.DatosSeguimientoBolsin;

import java.util.List;

/**
 * PATRÓN OBSERVER - Observador.
 * Lo implementan PantallaSeguimientoBolsines (listado y filtros) e InterfazGoogleMaps (marcadores del mapa).
 * Modelo "push": el sujeto envía los datos de seguimiento en la notificación.
 */
public interface IObservadorSeguimiento {

    void actualizar(List<DatosSeguimientoBolsin> datosSeguimiento);
}
