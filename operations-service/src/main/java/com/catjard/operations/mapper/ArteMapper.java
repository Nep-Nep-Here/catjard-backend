package com.catjard.operations.mapper;

import com.catjard.operations.dto.ArteDTO;
import com.catjard.operations.model.Arte;

public class ArteMapper {

    private ArteMapper() {}

    public static ArteDTO toDTO(Arte a) {
        return new ArteDTO(
                a.getId(),
                a.getPedidoCodigo(),
                a.getVersion(),
                a.getNombreArchivo(),
                a.getFecha(),
                a.getEstado() != null ? a.getEstado().name() : null,
                a.getComentariosCliente()
        );
    }
}
