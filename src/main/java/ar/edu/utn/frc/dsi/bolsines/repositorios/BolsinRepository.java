package ar.edu.utn.frc.dsi.bolsines.repositorios;

import ar.edu.utn.frc.dsi.bolsines.entidades.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BolsinRepository extends JpaRepository<Bolsin, Long> {
}
