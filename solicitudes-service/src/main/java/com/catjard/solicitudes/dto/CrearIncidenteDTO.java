package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos del formulario de registro de un incidente.
// La prioridad NO se envia: el backend la deriva de la matriz Impacto x Urgencia.
public record CrearIncidenteDTO(
        @NotBlank @Size(max = 160) String titulo,
        String descripcion,
        @NotBlank @Size(max = 15) String origen,        // usuario / monitoreo / mesa_ayuda
        @Size(max = 200) String servicioAfectado,
        @NotBlank @Size(max = 20) String categoria,     // infraestructura / aplicaciones / base_datos / redes / seguridad / documentacion / otros
        @NotBlank @Size(max = 10) String impacto,       // bajo / medio / alto
        @NotBlank @Size(max = 10) String urgencia,      // bajo / medio / alto
        @Size(max = 150) String responsable,
        String diagnostico,
        String solucion,
        String evidencia
) {}
