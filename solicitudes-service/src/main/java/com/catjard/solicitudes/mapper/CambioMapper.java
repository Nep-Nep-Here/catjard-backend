package com.catjard.solicitudes.mapper;

import com.catjard.solicitudes.dto.CambioDTO;
import com.catjard.solicitudes.model.Cambio;

public class CambioMapper {

    private CambioMapper() {}

    private static String n(Enum<?> e) { return e != null ? e.name() : null; }

    public static CambioDTO toDTO(Cambio c) {
        return new CambioDTO(
                c.getId(),
                c.getCodigo(),
                c.getFecha(),
                c.getProyecto(),
                c.getVersion(),
                n(c.getTipoCambio()),
                n(c.getCategoria()),
                c.getTitulo(),
                c.getDescripcion(),
                n(c.getImpacto()),
                n(c.getPrioridad()),
                n(c.getEstado()),
                n(c.getEtapaCI()),
                c.getResponsable(),
                c.getSolicitanteEmail(),
                c.getAreaAfectada(),
                c.getRiesgo(),
                n(c.getNivelRiesgo()),
                c.getPlanPruebas(),
                c.getPlanRollback(),
                c.getJiraIssueKey(),
                c.getJiraUrl(),
                c.getFechaCreacion(),
                c.getFechaActualizacion()
        );
    }
}
