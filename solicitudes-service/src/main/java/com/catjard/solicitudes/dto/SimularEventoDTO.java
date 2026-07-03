package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Simula una lectura de metrica (para demos/pruebas sin esperar a que el Droplet
// se sature de verdad): pasa por el MISMO pipeline de clasificacion que las reales.
public record SimularEventoDTO(
        @NotBlank String metrica,  // cpu / memoria / disco / load / red_entrada / red_salida
        @NotNull Double valor
) {}
