package com.catjard.solicitudes.mapper;

import com.catjard.solicitudes.dto.EventoDTO;
import com.catjard.solicitudes.model.Evento;

public class EventoMapper {

    private EventoMapper() {}

    private static String n(Enum<?> e) { return e != null ? e.name() : null; }

    public static EventoDTO toDTO(Evento e) {
        return new EventoDTO(
                e.getId(),
                e.getCodigo(),
                e.getFechaHora(),
                e.getOrigen(),
                e.getDroplet(),
                n(e.getMetrica()),
                e.getValor(),
                e.getUnidad(),
                e.getUmbral(),
                n(e.getSeveridad()),
                e.getMensaje(),
                e.isGeneraIncidente(),
                e.getJustificacion(),
                e.getIncidenteId(),
                e.getIncidenteCodigo(),
                e.getAccionRecomendada(),
                e.getResponsable(),
                e.getTiempoMaximo(),
                e.getJiraIssueKey(),
                e.getJiraUrl(),
                n(e.getEstado()),
                e.getFechaCreacion(),
                e.getFechaActualizacion()
        );
    }
}
