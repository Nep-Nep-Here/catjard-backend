package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Size;

// El admin (gerente) actualiza estado, prioridad y/o responde la solicitud.
// Todos los campos son opcionales: se aplica solo lo que venga distinto de null.
public record ActualizarSolicitudDTO(
        @Size(max = 20) String estado,
        @Size(max = 10) String prioridad,
        String respuesta
) {}
