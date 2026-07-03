package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.EstadoEvento;
import com.catjard.solicitudes.model.Evento;
import com.catjard.solicitudes.model.SeveridadEvento;
import com.catjard.solicitudes.model.TipoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findAllByOrderByFechaHoraDescIdDesc();
    List<Evento> findBySeveridadOrderByFechaHoraDescIdDesc(SeveridadEvento severidad);
    List<Evento> findByEstadoOrderByFechaHoraDescIdDesc(EstadoEvento estado);
    List<Evento> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    // Ultimo evento registrado para una metrica: se usa para no duplicar alertas
    // mientras la condicion sigue igual (anti-ruido / cooldown).
    Optional<Evento> findTopByMetricaOrderByIdDesc(TipoMetrica metrica);
}
