package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Articulo de la Base de Conocimiento: planes de continuidad, DRP, respaldos,
// politicas y runbooks (estrategias de recuperacion paso a paso por escenario).
// Un runbook puede vincularse a una categoria de incidente y/o a un servicio del
// catalogo: asi el detalle del incidente sugiere la estrategia documentada
// ("se cayo la BD -> este es el procedimiento") y la referencia viaja a Jira.
@Entity
@Table(name = "articulos_kb")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticuloKB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // KB-2026-001

    @Column(nullable = false, length = 160)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private CategoriaKB categoria;

    @Column(length = 400)
    private String resumen;                // bajada corta para la tarjeta

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;              // el plan / procedimiento paso a paso

    @Column(length = 150)
    private String autor;

    @Column(nullable = false)
    private int vistas;

    // ----- Vinculos para sugerir la estrategia en un incidente -----
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_incidente", length = 20)
    private CategoriaIncidente categoriaIncidente;   // null = no aplica a incidentes

    @Column(name = "servicio_id")
    private Long servicioId;                          // servicio del catalogo (opcional)

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
