package ar.edu.utn.frc.dsi.bolsines.interfaces.observer;

import ar.edu.utn.frc.dsi.bolsines.entidades.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.interfaces.gps.UbicacionBolsin;

/**
 * PATRÓN OBSERVER - Observador del envío de la ubicación de un bolsín (Vista Dinámica - Observer).
 * Lo implementa InterfazNotificacionEmail: cuando el GestorUbicacionBolsines notifica, envía el correo al GCM.
 * Modelo "push": el sujeto envía en la notificación el bolsín, el email del GCM destino y la ubicación.
 */
public interface IObservadorBolsines {

    void actualizar(Bolsin bolsin, String emailGCM, UbicacionBolsin ubicacion);
}
