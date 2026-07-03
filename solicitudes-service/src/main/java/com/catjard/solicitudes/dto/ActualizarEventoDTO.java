package com.catjard.solicitudes.dto;

// Gestion del evento en el panel: avanzar estado o ajustar el plan de respuesta.
public record ActualizarEventoDTO(
        String estado,            // nuevo / en_revision / atendido / descartado
        String responsable,
        String accionRecomendada,
        String tiempoMaximo,
        String justificacion
) {}
