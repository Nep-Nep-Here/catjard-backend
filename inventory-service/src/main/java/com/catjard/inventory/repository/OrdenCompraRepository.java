package com.catjard.inventory.repository;

import com.catjard.inventory.model.EstadoOC;
import com.catjard.inventory.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    Optional<OrdenCompra> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<OrdenCompra> findByEstado(EstadoOC estado);
    List<OrdenCompra> findByProveedorIdOrderByFechaDescIdDesc(Long proveedorId);
    List<OrdenCompra> findAllByOrderByFechaDescIdDesc();
    List<OrdenCompra> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
}
