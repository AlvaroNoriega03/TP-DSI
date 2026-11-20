package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "comision_medica")
public class ComisionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código que usan los GPS Trackers para identificar la CM (ej. CM-CBA-01). */
    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String email;
    private String telefono;

    protected ComisionMedica() {
    }

    public ComisionMedica(String codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getId() {
        return id;
    }

    /** Dos objetos CM son la misma comisión si tienen el mismo código. */
    public boolean sosLaMisma(ComisionMedica otra) {
        return otra != null && Objects.equals(codigo, otra.codigo);
    }
}
