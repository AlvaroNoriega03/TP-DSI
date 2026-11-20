package ar.edu.utn.frc.dsi.bolsines.pantallas;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Infraestructura web (no es parte del dominio): canal Server-Sent Events entre las boundaries que corren en el
 * servidor (PantallaUbicacionBolsines, InterfazGoogleMaps) y el navegador del EB.
 * Gracias a este canal, cuando un observador recibe actualizar(...) el navegador se refresca solo.
 * Si el navegador todavía no se conectó, los eventos quedan pendientes y se envían al conectarse.
 */
@Component
@SessionScope
public class CanalEventosPantalla {

    private SseEmitter emitter;
    private final List<Object[]> pendientes = new ArrayList<>();

    public synchronized SseEmitter conectar() {
        SseEmitter nuevo = new SseEmitter(0L); // sin timeout
        nuevo.onCompletion(() -> desconectar(nuevo));
        nuevo.onTimeout(() -> desconectar(nuevo));
        nuevo.onError(e -> desconectar(nuevo));
        this.emitter = nuevo;
        // Se escribe algo apenas se conecta para que se envíen los headers de la respuesta:
        // si no, el navegador nunca dispara onopen hasta que llegue el primer evento real.
        try {
            nuevo.send(SseEmitter.event().comment("conectado"));
        } catch (IOException e) {
            this.emitter = null;
        }
        List<Object[]> aEnviar = new ArrayList<>(pendientes);
        pendientes.clear();
        for (Object[] evento : aEnviar) {
            enviar((String) evento[0], evento[1]);
        }
        return nuevo;
    }

    public synchronized void enviar(String nombreEvento, Object datos) {
        if (emitter == null) {
            pendientes.add(new Object[]{nombreEvento, datos});
            return;
        }
        try {
            emitter.send(SseEmitter.event().name(nombreEvento).data(datos));
        } catch (IOException | IllegalStateException e) {
            emitter = null;
            pendientes.add(new Object[]{nombreEvento, datos});
        }
    }

    private synchronized void desconectar(SseEmitter cerrado) {
        if (this.emitter == cerrado) {
            this.emitter = null;
        }
    }
}
