package com.catjard.sales.repository;

import com.catjard.sales.model.Cotizacion;
import com.catjard.sales.model.EstadoCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    Optional<Cotizacion> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Cotizacion> findByEstado(EstadoCotizacion estado);
    List<Cotizacion> findByClienteIdOrderByFechaDescIdDesc(Long clienteId);
    List<Cotizacion> findAllByOrderByFechaDescIdDesc();
    List<Cotizacion> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
}
