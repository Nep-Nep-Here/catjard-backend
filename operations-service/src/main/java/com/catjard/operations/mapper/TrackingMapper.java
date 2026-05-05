package com.catjard.operations.mapper;

import com.catjard.operations.dto.TrackingEventoDTO;
import com.catjard.operations.model.TrackingEvento;

public class TrackingMapper {

    private TrackingMapper() {}

    public static TrackingEventoDTO toDTO(TrackingEvento t) {
        return new TrackingEventoDTO(
                t.getId(),
                t.getPedidoCodigo(),
                t.getHito() != null ? t.getHito().name() : null,
                t.getFecha(),
                t.getCompleto(),
                t.getObservacion()
        );
    }
}
