package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Catalogo de servicios criticos de Cat Jard (Fases 1 y 3 de la Gestion de
// Continuidad): criticidad, prioridad de recuperacion y objetivos RTO/RPO.
// Todo lo demas del modulo (riesgos, respaldos, contador RTO de incidentes)
// se cuelga de este catalogo.
@Entity
@Table(name = "servicios_criticos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioCritico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // SRV-001

    @Column(nullable = false, length = 120)
    private String nombre;                 // "API Gateway", "Base de datos PostgreSQL"...

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoServicio tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CriticidadServicio criticidad;

    @Column(name = "prioridad_recuperacion", nullable = false)
    private Integer prioridadRecuperacion; // 1 = se recupera primero

    @Column(name = "rto_minutos")
    private Integer rtoMinutos;            // tiempo maximo de recuperacion (objetivo)

    @Column(name = "rpo_minutos")
    private Integer rpoMinutos;            // perdida maxima de datos (objetivo); null = sin datos propios

    @Column(name = "estrategia_continuidad", columnDefinition = "TEXT")
    private String estrategiaContinuidad;  // HA / respaldos / mitigacion aplicada

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (prioridadRecuperacion == null) prioridadRecuperacion = 99;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
