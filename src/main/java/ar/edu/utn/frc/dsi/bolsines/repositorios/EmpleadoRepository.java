package ar.edu.utn.frc.dsi.bolsines.repositorios;

import ar.edu.utn.frc.dsi.bolsines.entidades.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entidades.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    /** Empleados asignados a una comisión médica (se usa para buscar el GCM de la CM destino). */
    List<Empleado> findByComisionMedica(ComisionMedica comisionMedica);
}
