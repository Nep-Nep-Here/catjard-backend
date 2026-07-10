package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Size;

// Gestion del incidente: avanzar el flujo, reclasificar, registrar diagnostico/solucion.
// Todos los campos son opcionales: se aplica solo lo que venga.
// Si cambia impacto o urgencia, la prioridad se recalcula con la matriz.
public record ActualizarIncidenteDTO(
        @Size(max = 20) String estado,        // en_diagnostico / en_resolucion / resuelto / cerrado / reabierto / cancelado
        @Size(max = 20) String categoria,
        @Size(max = 10) String impacto,
        @Size(max = 10) String urgencia,
        @Size(max = 150) String responsable,
        @Size(max = 200) String servicioAfectado,
        Long servicioId,                      // (re)asociar al catalogo de continuidad: recalcula el deadline RTO
        String diagnostico,
        String solucion,
        String evidencia
) {}
