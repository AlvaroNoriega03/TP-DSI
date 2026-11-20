package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "clave_usuario", nullable = false)
    private String claveUsuario;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String claveUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.claveUsuario = claveUsuario;
    }

    /** Valida la clave ingresada en el login (en el prototipo se guarda en texto plano). */
    public boolean esTuClave(String clave) {
        return claveUsuario != null && claveUsuario.equals(clave);
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}
