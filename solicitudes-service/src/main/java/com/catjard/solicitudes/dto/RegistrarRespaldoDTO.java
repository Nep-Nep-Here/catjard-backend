package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Registro de una ejecucion de respaldo. Lo envia el cron del Droplet
// (endpoint con token) o el panel de gerencia (manual).
public record RegistrarRespaldoDTO(
        LocalDateTime fechaHora,                       // null = ahora
        Long servicioId,                               // servicio critico respaldado (opcional)
        @NotBlank @Size(max = 160) String recurso,
        @NotBlank @Size(max = 15) String tipo,         // completo / incremental / snapshot
        @NotBlank @Size(max = 20) String destino,      // droplet_local / snapshot_do / copia_externa
        @NotBlank @Size(max = 10) String estado,       // exitoso / fallido
        Double tamanoMb,
        Integer duracionSeg,
        @Size(max = 255) String mensaje
) {}
