package ar.edu.utn.frc.dsi.bolsines.interfaces.mapa;

import ar.edu.utn.frc.dsi.bolsines.dto.DatosSeguimientoBolsin;
import ar.edu.utn.frc.dsi.bolsines.dto.MarcadorMapa;
import ar.edu.utn.frc.dsi.bolsines.interfaces.observer.IObservadorSeguimiento;
import ar.edu.utn.frc.dsi.bolsines.pantallas.CanalEventosPantalla;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Boundary hacia el Servidor Google Maps (RNF 7).
 * PATRÓN OBSERVER - Observador concreto: al recibir actualizar(...) arma los marcadores con las coordenadas
 * y los envía a la librería de mapas del navegador para que dibuje la posición de cada bolsín.
 */
@Component
@SessionScope
public class InterfazGoogleMaps implements IObservadorSeguimiento {

    private final CanalEventosPantalla canal;

    public InterfazGoogleMaps(CanalEventosPantalla canal) {
        this.canal = canal;
    }

    @Override
    public void actualizar(List<DatosSeguimientoBolsin> datosSeguimiento) {
        List<MarcadorMapa> marcadores = construirMarcadores(datosSeguimiento);
        renderizarMapa(marcadores);
    }

    private List<MarcadorMapa> construirMarcadores(List<DatosSeguimientoBolsin> datosSeguimiento) {
        List<MarcadorMapa> marcadores = new ArrayList<>();
        for (DatosSeguimientoBolsin datos : datosSeguimiento) {
            if (datos.ubicado()) {
                marcadores.add(new MarcadorMapa(
                        datos.numeroBolsin(),
                        datos.latitud(),
                        datos.longitud(),
                        "Bolsín N° " + datos.numeroBolsin(),
                        "Destino: " + datos.nombreCMDestino()
                                + " · Precinto: " + datos.numeroPrecinto()
                                + " · Últ. act.: " + datos.fechaHoraUltimaActualizacion()));
            }
        }
        return marcadores;
    }

    private void renderizarMapa(List<MarcadorMapa> marcadores) {
        canal.enviar("mapa", marcadores);
    }
}
