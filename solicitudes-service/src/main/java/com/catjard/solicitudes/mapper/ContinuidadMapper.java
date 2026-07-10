package com.catjard.solicitudes.mapper;

import com.catjard.solicitudes.dto.RespaldoDTO;
import com.catjard.solicitudes.dto.RiesgoDTO;
import com.catjard.solicitudes.dto.ServicioCriticoDTO;
import com.catjard.solicitudes.model.Respaldo;
import com.catjard.solicitudes.model.Riesgo;
import com.catjard.solicitudes.model.ServicioCritico;

public class ContinuidadMapper {

    private ContinuidadMapper() {}

    private static String n(Enum<?> e) { return e != null ? e.name() : null; }

    public static ServicioCriticoDTO toDTO(ServicioCritico s) {
        return new ServicioCriticoDTO(
                s.getId(),
                s.getCodigo(),
                s.getNombre(),
                s.getDescripcion(),
                n(s.getTipo()),
                n(s.getCriticidad()),
                s.getPrioridadRecuperacion(),
                s.getRtoMinutos(),
                s.getRpoMinutos(),
                s.getEstrategiaContinuidad(),
                s.isActivo(),
                s.getFechaCreacion(),
                s.getFechaActualizacion()
        );
    }

    public static RiesgoDTO toDTO(Riesgo r) {
        return new RiesgoDTO(
                r.getId(),
                r.getCodigo(),
                r.getNombre(),
                r.getDescripcion(),
                n(r.getProbabilidad()),
                n(r.getImpacto()),
                n(r.getNivelRiesgo()),
                r.getAccionMitigacion(),
                n(r.getEstado()),
                r.getServicios().stream()
                        .map(s -> new RiesgoDTO.ServicioResumenDTO(
                                s.getId(), s.getCodigo(), s.getNombre(), n(s.getCriticidad())))
                        .toList(),
                r.getFechaCreacion(),
                r.getFechaActualizacion()
        );
    }

    // El nombre del servicio se resuelve en el service (la entidad guarda solo el id).
    public static RespaldoDTO toDTO(Respaldo r, String servicioNombre) {
        return new RespaldoDTO(
                r.getId(),
                r.getCodigo(),
                r.getFechaHora(),
                r.getServicioId(),
                servicioNombre,
                r.getRecurso(),
                n(r.getTipo()),
                n(r.getDestino()),
                n(r.getEstado()),
                r.getTamanoMb(),
                r.getDuracionSeg(),
                r.getMensaje(),
                n(r.getOrigen()),
                r.getFechaCreacion()
        );
    }
}
