package com.catjard.sales.repository;

import com.catjard.sales.model.EstadoPedido;
import com.catjard.sales.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByClienteIdOrderByFechaPedidoDescIdDesc(Long clienteId);
    List<Pedido> findAllByOrderByFechaPedidoDescIdDesc();
    List<Pedido> findByCodigoStartingWithOrderByCodigoDesc(String prefijo);
    Optional<Pedido> findByCotizacionCodigo(String cotizacionCodigo);
}
