package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.EstadoSolicitud;
import com.catjard.solicitudes.model.Solicitud;
import com.catjard.solicitudes.model.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    Optional<Solicitud> findByCodigo(String codigo);
    List<Solicitud> findAllByOrderByFechaDescIdDesc();
    List<Solicitud> findByEstadoOrderByFechaDescIdDesc(EstadoSolicitud estado);
    List<Solicitud> findByTipoOrderByFechaDescIdDesc(TipoSolicitud tipo);
    List<Solicitud> findBySolicitanteEmailIgnoreCaseOrderByIdDesc(String email);
    List<Solicitud> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
}
