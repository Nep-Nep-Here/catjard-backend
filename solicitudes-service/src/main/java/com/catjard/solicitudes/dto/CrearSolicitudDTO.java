package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lo que el usuario envia desde la pagina de Ayuda.
// solicitanteEmail/Nombre solo se usan en envios PUBLICOS (sin login); si hay JWT,
// el email se toma del token y estos campos se ignoran.
public record CrearSolicitudDTO(
        @NotBlank @Size(max = 20) String tipo,
        @NotBlank @Size(max = 160) String asunto,
        @NotBlank String descripcion,
        @Size(max = 10) String prioridad,
        @Email @Size(max = 150) String solicitanteEmail,
        @Size(max = 150) String solicitanteNombre
) {}
