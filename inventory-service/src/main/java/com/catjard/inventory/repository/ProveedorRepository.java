package com.catjard.inventory.repository;

import com.catjard.inventory.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRuc(String ruc);
    boolean existsByRuc(String ruc);
    List<Proveedor> findByActivo(Boolean activo);
    List<Proveedor> findAllByOrderByRazonSocialAsc();
}
