package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.EstadoRespaldo;
import com.catjard.solicitudes.model.Respaldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespaldoRepository extends JpaRepository<Respaldo, Long> {
    List<Respaldo> findTop100ByOrderByFechaHoraDescIdDesc();
    List<Respaldo> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    // Ultimo respaldo exitoso de un servicio: base del semaforo RPO (tiempo
    // transcurrido vs. RPO objetivo del catalogo).
    Optional<Respaldo> findTopByServicioIdAndEstadoOrderByFechaHoraDesc(Long servicioId, EstadoRespaldo estado);
    // Dedupe del sync con DigitalOcean (id de la imagen en DO).
    boolean existsByExternoId(String externoId);
}
