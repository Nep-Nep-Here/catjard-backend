package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.ServicioCritico;
import com.catjard.solicitudes.model.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioCriticoRepository extends JpaRepository<ServicioCritico, Long> {
    List<ServicioCritico> findAllByOrderByPrioridadRecuperacionAscIdAsc();
    List<ServicioCritico> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    // Servicio que representa la infraestructura (Droplet): a el se cuelgan los
    // incidentes auto-creados por el monitoreo para fijar su deadline RTO.
    Optional<ServicioCritico> findFirstByTipoAndActivoTrueOrderByIdAsc(TipoServicio tipo);
}
