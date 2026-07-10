package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.ArticuloKB;
import com.catjard.solicitudes.model.CategoriaIncidente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticuloKBRepository extends JpaRepository<ArticuloKB, Long> {
    List<ArticuloKB> findAllByOrderByFechaActualizacionDescIdDesc();
    List<ArticuloKB> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    // Estrategias documentadas que aplican a un incidente (por categoria o servicio).
    List<ArticuloKB> findByCategoriaIncidenteOrderByIdAsc(CategoriaIncidente categoria);
    List<ArticuloKB> findByServicioIdOrderByIdAsc(Long servicioId);
}
