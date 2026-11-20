package ar.edu.utn.frc.dsi.bolsines.interfaces.observer;

/**
 * PATRÓN OBSERVER - Sujeto del envío de la ubicación de un bolsín (Vista Dinámica - Observer).
 * Lo implementa GestorUbicacionBolsines: al confirmar el envío del correo suscribe a los observadores
 * (InterfazNotificacionEmail) y los notifica.
 */
public interface ISujetoBolsines {

    void suscribir(IObservadorBolsines[] observadores);

    void quitar(IObservadorBolsines observador);

    void notificar();
}
