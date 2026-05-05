package com.catjard.operations.repository;

import com.catjard.operations.model.HitoTracking;
import com.catjard.operations.model.TrackingEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingEventoRepository extends JpaRepository<TrackingEvento, Long> {
    List<TrackingEvento> findByPedidoCodigoOrderByIdAsc(String pedidoCodigo);
    Optional<TrackingEvento> findByPedidoCodigoAndHito(String pedidoCodigo, HitoTracking hito);
}
