package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Plan / registro de Control de Cambios (un cambio = una "solicitud de cambio").
@Entity
@Table(name = "cambios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // CHN-2026-001

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 160)
    private String proyecto;

    @Column(length = 10)
    private String version;                // 1.0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoCambio tipoCambio;         // estandar / normal / emergencia

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaCambio categoria;     // infraestructura / aplicaciones / documentacion / calendario

    @Column(nullable = false, length = 160)
    private String titulo;                 // "cambio solicitado" (resumen)

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Nivel impacto;                 // bajo / medio / alto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;           // baja / media / alta (reutilizado)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCambio estado;           // flujo de aprobacion

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_ci", nullable = false, length = 15)
    private EtapaCI etapaCI;               // pipeline CI tras aprobacion

    @Column(length = 150)
    private String responsable;

    @Column(name = "solicitante_email", nullable = false, length = 150)
    private String solicitanteEmail;

    // ----- Evaluacion de impacto -----
    @Column(name = "area_afectada", length = 200)
    private String areaAfectada;

    @Column(columnDefinition = "TEXT")
    private String riesgo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", length = 10)
    private Nivel nivelRiesgo;

    // ----- Planes -----
    @Column(name = "plan_pruebas", columnDefinition = "TEXT")
    private String planPruebas;

    @Column(name = "plan_rollback", columnDefinition = "TEXT")
    private String planRollback;

    // ----- Integracion con Jira (gestion de cambios) -----
    @Column(name = "jira_issue_key", length = 40)
    private String jiraIssueKey;

    @Column(name = "jira_url", length = 255)
    private String jiraUrl;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (fecha == null) fecha = LocalDate.now();
        if (estado == null) estado = EstadoCambio.solicitado;
        if (etapaCI == null) etapaCI = EtapaCI.pendiente;
        if (prioridad == null) prioridad = Prioridad.media;
        if (version == null) version = "1.0";
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
