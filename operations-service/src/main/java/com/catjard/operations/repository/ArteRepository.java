package com.catjard.operations.repository;

import com.catjard.operations.model.Arte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArteRepository extends JpaRepository<Arte, Long> {
    List<Arte> findByPedidoCodigoOrderByVersionAsc(String pedidoCodigo);
    Optional<Arte> findByPedidoCodigoAndVersion(String pedidoCodigo, Integer version);
    Optional<Arte> findFirstByPedidoCodigoOrderByVersionDesc(String pedidoCodigo);
}
