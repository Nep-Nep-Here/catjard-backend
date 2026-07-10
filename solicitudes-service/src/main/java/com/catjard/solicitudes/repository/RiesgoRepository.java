package com.catjard.solicitudes.repository;

import com.catjard.solicitudes.model.EstadoRiesgo;
import com.catjard.solicitudes.model.Riesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {
    // El orden por nivel se hace en el service (en BD el enum es varchar y
    // ordenaria alfabeticamente: alto < bajo < critico < medio).
    List<Riesgo> findAllByOrderByIdAsc();
    List<Riesgo> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    long countByEstadoIn(List<EstadoRiesgo> estados);
}
