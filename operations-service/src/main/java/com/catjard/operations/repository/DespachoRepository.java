package com.catjard.operations.repository;

import com.catjard.operations.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    Optional<Despacho> findByPedidoCodigo(String pedidoCodigo);
    boolean existsByPedidoCodigo(String pedidoCodigo);
    List<Despacho> findAllByOrderByFechaDespachoDescIdDesc();
}
