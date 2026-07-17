package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// Edicion de un servicio del catalogo: los campos no nulos se aplican. Excepcion:
// rpoMinutos se aplica tal cual (incluido null) para poder QUITAR el objetivo RPO
// desde el formulario (servicio sin datos propios -> sale del semaforo).
public record ActualizarServicioCriticoDTO(
        @Size(max = 120) String nombre,
        String descripcion,
        @Size(max = 20) String tipo,
        @Size(max = 10) String criticidad,
        @Min(1) @Max(99) Integer prioridadRecuperacion,
        @Min(1) Integer rtoMinutos,
        @Min(1) Integer rpoMinutos,
        String estrategiaContinuidad,
        Boolean activo
) {}
