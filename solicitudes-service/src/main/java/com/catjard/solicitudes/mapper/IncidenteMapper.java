package com.catjard.solicitudes.mapper;

import com.catjard.solicitudes.dto.IncidenteDTO;
import com.catjard.solicitudes.model.Incidente;

public class IncidenteMapper {

    private IncidenteMapper() {}

    private static String n(Enum<?> e) { return e != null ? e.name() : null; }

    public static IncidenteDTO toDTO(Incidente i) {
        return new IncidenteDTO(
                i.getId(),
                i.getCodigo(),
                i.getFecha(),
                i.getTitulo(),
                i.getDescripcion(),
                n(i.getOrigen()),
                i.getServicioAfectado(),
                n(i.getCategoria()),
                n(i.getImpacto()),
                n(i.getUrgencia()),
                n(i.getPrioridad()),
                n(i.getEstado()),
                i.getResponsable(),
                i.getSolicitanteEmail(),
                i.getDiagnostico(),
                i.getSolucion(),
                i.getEvidencia(),
                i.getFechaResolucion(),
                i.getFechaCierre(),
                i.getJiraIssueKey(),
                i.getJiraUrl(),
                i.getFechaCreacion(),
                i.getFechaActualizacion()
        );
    }
}
