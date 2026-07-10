package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Alta de un articulo en la Base de Conocimiento.
public record CrearArticuloKBDTO(
        @NotBlank @Size(max = 160) String titulo,
        @NotBlank @Size(max = 25) String categoria,   // continuidad_servicio / recuperacion_desastres / respaldos / monitoreo_eventos / gestion_incidencias / runbook
        @Size(max = 400) String resumen,
        @NotBlank String contenido,
        @Size(max = 150) String autor,
        @Size(max = 20) String categoriaIncidente,    // opcional: a que categoria de incidente aplica
        Long servicioId                               // opcional: servicio del catalogo de continuidad
) {}
