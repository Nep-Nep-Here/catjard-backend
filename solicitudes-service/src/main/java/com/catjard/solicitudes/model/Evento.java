package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Monitoreo Estrategico y Gestion de Eventos: un registro por cada evento detectado
// en el Droplet (via API de Monitoring de DigitalOcean) o simulado para pruebas.
// Fases: deteccion -> clasificacion (severidad) -> identificacion (¿genera incidente?)
// -> plan de respuesta (accion / responsable / tiempo maximo).
@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // EVT-2026-001

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;       // momento de la deteccion

    @Column(nullable = false, length = 20)
    private String origen;                 // digitalocean / simulado

    @Column(length = 100)
    private String droplet;                // nombre del droplet monitoreado

    // ----- Deteccion -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoMetrica metrica;

    @Column(nullable = false)
    private Double valor;                  // valor medido

    @Column(nullable = false, length = 10)
    private String unidad;                 // % / Mbps / load

    private Double umbral;                 // umbral que se cruzo (null si informacion)

    // ----- Fase 1: Clasificacion -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SeveridadEvento severidad;

    @Column(nullable = false, length = 255)
    private String mensaje;                // "CPU al 95% (umbral critico 90%)"

    // ----- Fase 2: Identificacion (¿genera incidente?) -----
    @Column(name = "genera_incidente", nullable = false)
    private boolean generaIncidente;

    @Column(columnDefinition = "TEXT")
    private String justificacion;          // por que genera (o no) un incidente

    @Column(name = "incidente_id")
    private Long incidenteId;              // incidente auto-creado (error / critical)

    @Column(name = "incidente_codigo", length = 20)
    private String incidenteCodigo;

    // ----- Fase 5: Plan de respuesta (autodesignado por la clasificacion) -----
    @Column(name = "accion_recomendada", columnDefinition = "TEXT")
    private String accionRecomendada;

    @Column(length = 150)
    private String responsable;            // equipo autodesignado

    @Column(name = "tiempo_maximo", length = 30)
    private String tiempoMaximo;           // tiempo maximo de respuesta

    // ----- Integracion con Jira (boton "Enviar a Jira", solo criticos) -----
    @Column(name = "jira_issue_key", length = 40)
    private String jiraIssueKey;

    @Column(name = "jira_url", length = 255)
    private String jiraUrl;

    // ----- Flujo en el panel -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoEvento estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (fechaHora == null) fechaHora = now;
        if (estado == null) estado = EstadoEvento.nuevo;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
