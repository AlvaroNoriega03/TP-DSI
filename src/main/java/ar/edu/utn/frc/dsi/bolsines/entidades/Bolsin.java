package ar.edu.utn.frc.dsi.bolsines.entidades;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bolsin")
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bolsin", nullable = false, unique = true)
    private Integer numeroBolsin;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Peso expresado en gramos. */
    private Integer peso;

    @Column(name = "numero_precinto")
    private Integer numeroPrecinto;

    /**
     * Modelo del dispositivo GPS Tracker colocado en el bolsín (RNF 6).
     * Decisión de diseño: con este dato el gestor elige el adaptador (patrón Adapter).
     */
    @Column(name = "modelo_gps_tracker", nullable = false)
    private String modeloGPSTracker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica origen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica destino;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "bolsin_id")
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    protected Bolsin() {
    }

    public Bolsin(Integer numeroBolsin, LocalDate fecha, Integer peso, Integer numeroPrecinto,
                  String modeloGPSTracker, ComisionMedica origen, ComisionMedica destino) {
        this.numeroBolsin = numeroBolsin;
        this.fecha = fecha;
        this.peso = peso;
        this.numeroPrecinto = numeroPrecinto;
        this.modeloGPSTracker = modeloGPSTracker;
        this.origen = origen;
        this.destino = destino;
    }

    public boolean esTuCMOrigen(ComisionMedica cm) {
        return origen != null && origen.sosLaMisma(cm);
    }

    /** Recorre sus cambios de estado y pregunta al actual si es "Enviado". */
    public boolean sosEnviado() {
        for (CambioEstadoBolsin ce : cambioEstado) {
            if (ce.sosActual()) {
                return ce.sosEnviado();
            }
        }
        return false;
    }

    public boolean tenesPrecinto(Integer nroPrecinto) {
        return numeroPrecinto != null && numeroPrecinto.equals(nroPrecinto);
    }

    public boolean tenesCMDestino(String codigoCMDestino) {
        return destino != null && destino.getCodigo().equalsIgnoreCase(codigoCMDestino);
    }

    public void agregarCambioEstado(CambioEstadoBolsin nuevo) {
        cambioEstado.add(nuevo);
    }

    public Integer getNumeroBolsin() {
        return numeroBolsin;
    }

    public Integer getNumeroPrecinto() {
        return numeroPrecinto;
    }

    public String getModeloGPSTracker() {
        return modeloGPSTracker;
    }

    public ComisionMedica getCMOrigen() {
        return origen;
    }

    public ComisionMedica getCMDestino() {
        return destino;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Integer getPeso() {
        return peso;
    }
}
