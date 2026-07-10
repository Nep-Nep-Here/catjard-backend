package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RiesgoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String probabilidad,
        String impacto,
        String nivelRiesgo,
        String accionMitigacion,
        String estado,
        List<ServicioResumenDTO> servicios,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    // Referencia corta a un servicio afectado (para chips/listas del panel).
    public record ServicioResumenDTO(Long id, String codigo, String nombre, String criticidad) {}
}
