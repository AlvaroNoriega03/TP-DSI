package ar.edu.utn.frc.dsi.bolsines.config;

import ar.edu.utn.frc.dsi.bolsines.entidades.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entidades.CambioEstadoBolsin;
import ar.edu.utn.frc.dsi.bolsines.entidades.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entidades.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidades.Estado;
import ar.edu.utn.frc.dsi.bolsines.entidades.Rol;
import ar.edu.utn.frc.dsi.bolsines.entidades.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorios.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.RolRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos de prueba. Se cargan en H2 sólo si la base está vacía.
 * Los números de bolsín coinciden con los que conoce la API mock de GPS de la cátedra (101 a 110), y se eligió
 * un modelo de GPS por bolsín para poder mostrar los tres adaptadores y los flujos alternativos:
 *
 *  Usuario eb.cordoba (CM Córdoba, CM-CBA-01)
 *   - 101  XTR-4500L        Enviado -> CM Rosario      (flujo descripto del CU, con XTR-4500L)
 *   - 110  NavTrack QX-7A   Enviado -> CM Rosario
 *   - 103  GeoPulse MTR-900 Enviado -> CMC Buenos Aires
 *   - 107  GeoPulse MTR-900 Enviado -> CM Mar del Plata
 *   - 111  XTR-4500L        Enviado -> CM Tucumán       (la API no lo conoce: A3, el GPS no informa ubicación)
 *   - 120  XTR-4500L        Cerrado (no se muestra: no está Enviado)
 *  Usuario eb.jujuy (CM Jujuy) no tiene bolsines enviados: A1.
 */
@Component
public class CargaDatosIniciales implements CommandLineRunner {

    private final ComisionMedicaRepository cmRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;
    private final BolsinRepository bolsinRepository;

    public CargaDatosIniciales(ComisionMedicaRepository cmRepository, RolRepository rolRepository,
                               UsuarioRepository usuarioRepository, EmpleadoRepository empleadoRepository,
                               EstadoRepository estadoRepository, BolsinRepository bolsinRepository) {
        this.cmRepository = cmRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.estadoRepository = estadoRepository;
        this.bolsinRepository = bolsinRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (bolsinRepository.count() > 0) {
            return;
        }

        // Comisiones médicas (códigos iguales a los de la API mock)
        ComisionMedica cba = cm("CM-CBA-01", "Comisión Médica Córdoba", "Av. Colón 1234, Córdoba");
        ComisionMedica ros = cm("CM-ROS-02", "Comisión Médica Rosario", "Bv. Oroño 850, Rosario");
        ComisionMedica cmc = cm("CM-BA-01", "Comisión Médica Central (CABA)", "Av. Corrientes 2000, CABA");
        ComisionMedica mdq = cm("CM-MDQ-01", "Comisión Médica Mar del Plata", "Av. Colón 3100, Mar del Plata");
        ComisionMedica tuc = cm("CM-TUC-01", "Comisión Médica Tucumán", "San Martín 500, San Miguel de Tucumán");
        ComisionMedica jujuy = cm("CM-JUJ-01", "Comisión Médica Jujuy", "Belgrano 700, San Salvador de Jujuy");
        cm("CM-MDZ-03", "Comisión Médica Mendoza", "San Martín 1100, Mendoza");

        // Roles
        Rol eb = rolRepository.save(new Rol("Encargado de Bolsines", "Gestiona bolsines, su estado, retiro, recepción y seguimiento"));
        Rol gcm = rolRepository.save(new Rol(Rol.GERENTE_COMISION_MEDICA, "Gerente de la comisión médica"));

        // Usuarios y empleados
        Usuario uCba = usuarioRepository.save(new Usuario("eb.cordoba", "1234"));
        Usuario uJujuy = usuarioRepository.save(new Usuario("eb.jujuy", "1234"));
        Empleado ebCordoba = empleadoRepository.save(new Empleado("Lucía", "Fernández", "lfernandez@cm-cordoba.gob.ar", uCba, eb, cba));
        empleadoRepository.save(new Empleado("Martín", "Gómez", "mgomez@cm-jujuy.gob.ar", uJujuy, eb, jujuy));
        empleadoRepository.save(new Empleado("Carla", "Ruiz", "gerencia@cm-rosario.gob.ar", null, gcm, ros));
        empleadoRepository.save(new Empleado("Jorge", "Paz", "gerencia@cmc-caba.gob.ar", null, gcm, cmc));
        empleadoRepository.save(new Empleado("Ana", "Sosa", "gerencia@cm-mardelplata.gob.ar", null, gcm, mdq));
        empleadoRepository.save(new Empleado("Raúl", "Díaz", "gerencia@cm-tucuman.gob.ar", null, gcm, tuc));
        empleadoRepository.save(new Empleado("Laura", "Vega", "gerencia@cm-cordoba.gob.ar", null, gcm, cba));

        // Estados del ámbito Bolsín (máquina de estados de la E1)
        Estado creado = estado("Creado");
        Estado cerrado = estado("Cerrado");
        Estado enviado = estado(Estado.ENVIADO);
        estado("Recibido en CM destino");
        estado("De baja");

        // Bolsines
        bolsin(101, 1520, 50101, "XTR-4500L", cba, ros, ebCordoba, creado, cerrado, enviado);
        bolsin(110, 980, 50110, "NavTrack QX-7A", cba, ros, ebCordoba, creado, cerrado, enviado);
        bolsin(103, 2300, 50103, "GeoPulse MTR-900", cba, cmc, ebCordoba, creado, cerrado, enviado);
        bolsin(107, 640, 50107, "GeoPulse MTR-900", cba, mdq, ebCordoba, creado, cerrado, enviado);
        bolsin(111, 1100, 50111, "XTR-4500L", cba, tuc, ebCordoba, creado, cerrado, enviado);
        bolsin(120, 700, 50120, "XTR-4500L", cba, ros, ebCordoba, creado, cerrado);
    }

    private ComisionMedica cm(String codigo, String nombre, String direccion) {
        String sufijo = codigo.toLowerCase().replace("cm-", "");
        return cmRepository.save(new ComisionMedica(codigo, nombre, direccion, "mesa." + sufijo + "@cm.gob.ar", "0800-555-0000"));
    }

    private Estado estado(String nombre) {
        return estadoRepository.save(new Estado(nombre, Estado.AMBITO_BOLSIN, "Estado " + nombre + " del bolsín"));
    }

    /** Crea el bolsín con su historial de cambios de estado; el último queda como actual (sin fecha fin). */
    private void bolsin(int numero, int peso, int precinto, String modelo, ComisionMedica origen, ComisionMedica destino,
                        Empleado responsable, Estado... historial) {
        Bolsin bolsin = new Bolsin(numero, LocalDate.of(2026, 3, 6), peso, precinto, modelo, origen, destino);
        LocalDateTime inicio = LocalDateTime.of(2026, 3, 6, 9, 0);
        CambioEstadoBolsin anterior = null;
        for (Estado estado : historial) {
            if (anterior != null) {
                anterior.setFechaHoraFin(inicio);
            }
            CambioEstadoBolsin ce = new CambioEstadoBolsin(inicio, estado, responsable);
            bolsin.agregarCambioEstado(ce);
            anterior = ce;
            inicio = inicio.plusHours(4);
        }
        bolsinRepository.save(bolsin);
    }
}
