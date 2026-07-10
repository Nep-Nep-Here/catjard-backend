package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.EstadoIncidente;
import com.catjard.solicitudes.model.Incidente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidenteRepository extends JpaRepository<Incidente, Long> {
    Optional<Incidente> findByCodigo(String codigo);
    List<Incidente> findAllByOrderByFechaDescIdDesc();
    List<Incidente> findByEstadoOrderByFechaDescIdDesc(EstadoIncidente estado);
    List<Incidente> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    // Incidentes con contador RTO: base de las metricas de cumplimiento del plan.
    List<Incidente> findByRtoDeadlineIsNotNull();
}
