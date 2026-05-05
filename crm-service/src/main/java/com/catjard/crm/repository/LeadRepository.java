package com.catjard.crm.repository;

import com.catjard.crm.model.EstadoLead;
import com.catjard.crm.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Lead> findByEstado(EstadoLead estado);
    List<Lead> findAllByOrderByFechaDescIdDesc();
    List<Lead> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
}
