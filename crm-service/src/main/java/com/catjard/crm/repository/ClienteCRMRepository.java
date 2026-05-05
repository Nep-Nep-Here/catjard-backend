package com.catjard.crm.repository;

import com.catjard.crm.model.ClienteCRM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteCRMRepository extends JpaRepository<ClienteCRM, Long> {
    Optional<ClienteCRM> findByRuc(String ruc);
    boolean existsByRuc(String ruc);
    List<ClienteCRM> findByCuentaActiva(Boolean cuentaActiva);
    List<ClienteCRM> findAllByOrderByRazonSocialAsc();
}
