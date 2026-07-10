package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

// Matriz de riesgos (Fase 2): Riesgo - Probabilidad - Impacto - Accion de mitigacion,
// vinculado a los servicios criticos que afectaria. El nivel de riesgo se deriva
// de la matriz Probabilidad x Impacto (no lo envia el formulario).
@Entity
@Table(name = "riesgos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // RSG-2026-001

    @Column(nullable = false, length = 160)
    private String nombre;                 // "Caida del Droplet", "Ransomware"...

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel probabilidad;            // bajo / medio / alto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel impacto;                 // bajo / medio / alto

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 10)
    private NivelRiesgo nivelRiesgo;       // derivado: bajo / medio / alto / critico

    @Column(name = "accion_mitigacion", columnDefinition = "TEXT")
    private String accionMitigacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRiesgo estado;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "riesgo_servicios",
            joinColumns = @JoinColumn(name = "riesgo_id"),
            inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    @Builder.Default
    private Set<ServicioCritico> servicios = new LinkedHashSet<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (estado == null) estado = EstadoRiesgo.identificado;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
