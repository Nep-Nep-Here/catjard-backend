package com.catjard.inventory.mapper;

import com.catjard.inventory.dto.MovimientoDTO;
import com.catjard.inventory.model.Movimiento;

public class MovimientoMapper {

    private MovimientoMapper() {}

    public static MovimientoDTO toDTO(Movimiento m) {
        return new MovimientoDTO(
                m.getId(),
                m.getFecha(),
                m.getTipo() != null ? m.getTipo().name() : null,
                m.getProductoId(),
                m.getCantidad(),
                m.getMotivo(),
                m.getReferencia(),
                m.getUsuario(),
                m.getNotas()
        );
    }
}
