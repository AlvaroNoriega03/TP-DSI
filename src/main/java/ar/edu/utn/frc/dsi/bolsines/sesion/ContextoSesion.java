package ar.edu.utn.frc.dsi.bolsines.sesion;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/** Guarda, por cada navegador (sesión HTTP), el id de la Sesion persistida del usuario logueado. */
@Component
@SessionScope
public class ContextoSesion {

    private Long idSesion;

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public boolean hayUsuarioLogueado() {
        return idSesion != null;
    }
}
