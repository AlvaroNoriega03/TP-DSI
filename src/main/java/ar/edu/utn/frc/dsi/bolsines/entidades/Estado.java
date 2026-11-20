package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado")
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String ENVIADO = "Enviado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String ambito;

    private String descripcion;

    protected Estado() {
    }

    public Estado(String nombre, String ambito, String descripcion) {
        this.nombre = nombre;
        this.ambito = ambito;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equalsIgnoreCase(ambito);
    }

    public boolean esEnviado() {
        return ENVIADO.equalsIgnoreCase(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public String getAmbito() {
        return ambito;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
