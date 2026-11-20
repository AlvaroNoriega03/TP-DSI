package ar.edu.utn.frc.dsi.bolsines.interfaces.observer;

/**
 * PATRÓN OBSERVER - Sujeto.
 * Lo implementa GestorSeguimientoBolsines: cada vez que obtiene (u actualiza / filtra) la ubicación
 * de los bolsines, avisa a todos los observadores suscriptos sin conocer sus clases concretas.
 */
public interface ISujetoSeguimiento {

    void suscribir(IObservadorSeguimiento observador);

    void quitar(IObservadorSeguimiento observador);

    void notificar();
}
