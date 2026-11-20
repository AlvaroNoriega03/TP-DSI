package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String email;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    /** Comisión médica a la que está asignado el empleado (asignadoA). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "comision_medica_id")
    private ComisionMedica comisionMedica;

    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String email, Usuario usuario, Rol rol, ComisionMedica comisionMedica) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.usuario = usuario;
        this.rol = rol;
        this.comisionMedica = comisionMedica;
    }

    public boolean esTuUsuario(Usuario usuarioLogueado) {
        return usuario != null && usuarioLogueado != null && Objects.equals(usuario.getId(), usuarioLogueado.getId());
    }

    public ComisionMedica getCM() {
        return comisionMedica;
    }

    public boolean esTuCM(ComisionMedica cm) {
        return comisionMedica != null && comisionMedica.sosLaMisma(cm);
    }

    /** Delega en el Rol (Experto). */
    public boolean sosGCM() {
        return rol != null && rol.esTuGCM();
    }

    public String getEmail() {
        return email;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
