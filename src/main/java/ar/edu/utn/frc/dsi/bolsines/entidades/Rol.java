package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol {

    public static final String GERENTE_COMISION_MEDICA = "Gerente de Comisión Médica";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    protected Rol() {
    }

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** ¿Este rol es el de Gerente de Comisión Médica (GCM)? */
    public boolean esTuGCM() {
        return GERENTE_COMISION_MEDICA.equalsIgnoreCase(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
