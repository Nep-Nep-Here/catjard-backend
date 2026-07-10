package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Size;

// Edicion parcial de un articulo: solo se aplican los campos no nulos.
// categoriaIncidente/servicioId con cadena vacia o 0 = desvincular.
public record ActualizarArticuloKBDTO(
        @Size(max = 160) String titulo,
        @Size(max = 25) String categoria,
        @Size(max = 400) String resumen,
        String contenido,
        @Size(max = 150) String autor,
        @Size(max = 20) String categoriaIncidente,
        Long servicioId
) {}
