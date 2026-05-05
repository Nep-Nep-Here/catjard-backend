package com.catjard.operations.mapper;

import com.catjard.operations.dto.DespachoDTO;
import com.catjard.operations.model.Despacho;

public class DespachoMapper {

    private DespachoMapper() {}

    public static DespachoDTO toDTO(Despacho d) {
        return new DespachoDTO(
                d.getId(),
                d.getPedidoCodigo(),
                d.getCourier(),
                d.getGuiaRemision(),
                d.getFechaDespacho(),
                d.getFechaEntregaReal(),
                d.getDireccionEntrega(),
                d.getReceptor(),
                d.getNotas()
        );
    }
}
