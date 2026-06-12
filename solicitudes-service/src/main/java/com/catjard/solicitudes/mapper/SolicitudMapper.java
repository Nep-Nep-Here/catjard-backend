package com.catjard.solicitudes.mapper;

import com.catjard.solicitudes.dto.SolicitudDTO;
import com.catjard.solicitudes.model.Solicitud;

public class SolicitudMapper {

    private SolicitudMapper() {}

    public static SolicitudDTO toDTO(Solicitud s) {
        return new SolicitudDTO(
                s.getId(),
                s.getCodigo(),
                s.getFecha(),
                s.getTipo() != null ? s.getTipo().name() : null,
                s.getAsunto(),
                s.getDescripcion(),
                s.getPrioridad() != null ? s.getPrioridad().name() : null,
                s.getEstado() != null ? s.getEstado().name() : null,
                s.getSolicitanteEmail(),
                s.getSolicitanteNombre(),
                s.getRespuesta(),
                s.getAtendidoPor(),
                s.getJiraIssueKey(),
                s.getJiraUrl(),
                s.getFechaCreacion(),
                s.getFechaActualizacion()
        );
    }
}
