package ar.edu.utn.frc.dsi.bolsines.dto;

/** Marcador que InterfazGoogleMaps envía a la librería de mapas del navegador. */
public record MarcadorMapa(Integer numeroBolsin,
                           Double latitud,
                           Double longitud,
                           String titulo,
                           String detalle) {
}
