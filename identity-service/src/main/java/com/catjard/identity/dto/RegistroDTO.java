package com.catjard.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// MECANISMO DE SEGURIDAD: validación declarativa con Jakarta Bean Validation.
// Cada anotación impone una restricción que se valida al entrar la request al
// controller (gracias al @Valid). Esto evita datos basura y reduce la superficie
// de ataque (inyección, overflow, valores vacíos).
public record RegistroDTO(
        @NotBlank @Email String email,                    // formato de correo + no vacío
        @NotBlank @Size(min = 6, max = 100) String password, // password mínimo 6 caracteres
        @NotBlank String nombre,
        @NotBlank String empresa,
        @NotBlank @Size(min = 11, max = 11) String ruc,   // RUC peruano: exactamente 11 dígitos
        String telefono,
        String direccion
) {}
