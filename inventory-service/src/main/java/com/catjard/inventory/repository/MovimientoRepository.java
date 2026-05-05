package com.catjard.inventory.repository;

import com.catjard.inventory.model.Movimiento;
import com.catjard.inventory.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findAllByOrderByFechaDescIdDesc();
    List<Movimiento> findByProductoIdOrderByFechaDescIdDesc(Long productoId);
    List<Movimiento> findByTipoOrderByFechaDescIdDesc(TipoMovimiento tipo);
    List<Movimiento> findByReferenciaOrderByFechaDescIdDesc(String referencia);
}
