package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.Cambio;
import com.catjard.solicitudes.model.EstadoCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CambioRepository extends JpaRepository<Cambio, Long> {
    Optional<Cambio> findByCodigo(String codigo);
    List<Cambio> findAllByOrderByFechaDescIdDesc();
    List<Cambio> findByEstadoOrderByFechaDescIdDesc(EstadoCambio estado);
    List<Cambio> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
}
