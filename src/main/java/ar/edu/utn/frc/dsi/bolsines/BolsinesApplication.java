package ar.edu.utn.frc.dsi.bolsines;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PPAI 2026 - DSI - 3K4 - Grupo 9.
 * CU 36: Consultar seguimiento de bolsines.
 * Patrones de diseño aplicados: Adapter (GPS Trackers) y Observer (actualización de ubicaciones).
 */
@SpringBootApplication
public class BolsinesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BolsinesApplication.class, args);
    }
}
