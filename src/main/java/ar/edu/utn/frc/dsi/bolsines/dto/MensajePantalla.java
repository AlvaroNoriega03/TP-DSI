package ar.edu.utn.frc.dsi.bolsines.dto;

/** Mensaje que la pantalla muestra al EB. tipo: "info", "exito", "advertencia" o "error". */
public record MensajePantalla(String tipo, String texto) {
}
