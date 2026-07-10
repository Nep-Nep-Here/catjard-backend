package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Registro de un incidente (Gestion de Incidentes): un ticket = un incidente.
// Flujo: registrar -> diagnosticar (causa) -> resolver (solucion) -> validar -> cerrar.
@Entity
@Table(name = "incidentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // INC-2026-001

    @Column(nullable = false)
    private LocalDate fecha;

    // ----- Registro -----
    @Column(nullable = false, length = 160)
    private String titulo;                 // resumen del incidente

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrigenIncidente origen;        // usuario / monitoreo / mesa_ayuda

    @Column(name = "servicio_afectado", length = 200)
    private String servicioAfectado;       // "Portal institucional", "Impresora de oficina"...

    // ----- Clasificacion -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaIncidente categoria;

    // ----- Priorizacion (matriz Impacto x Urgencia) -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel impacto;                 // bajo / medio / alto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel urgencia;                // bajo / medio / alto

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadIncidente prioridad;  // derivada: baja / media / alta / critica

    // ----- Flujo -----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidente estado;

    @Column(length = 150)
    private String responsable;            // tecnico asignado

    @Column(name = "solicitante_email", nullable = false, length = 150)
    private String solicitanteEmail;       // reportante

    // ----- Diagnostico / Resolucion -----
    @Column(columnDefinition = "TEXT")
    private String diagnostico;            // analisis de causa raiz

    @Column(columnDefinition = "TEXT")
    private String solucion;               // solucion aplicada

    @Column(columnDefinition = "TEXT")
    private String evidencia;              // enlace / nota de evidencia

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // ----- Continuidad del servicio: contador RTO -----
    // Al asociar el incidente a un servicio del catalogo se congela su RTO y se
    // fija el deadline; al resolver se mide si se cumplio (trazabilidad del plan).
    @Column(name = "servicio_id")
    private Long servicioId;

    @Column(name = "servicio_nombre", length = 120)
    private String servicioNombre;

    @Column(name = "rto_minutos")
    private Integer rtoMinutos;            // RTO objetivo al momento de asociar

    @Column(name = "rto_deadline")
    private LocalDateTime rtoDeadline;     // creacion + RTO

    @Column(name = "cumplio_rto")
    private Boolean cumplioRto;            // null = aun sin medir

    // ----- Integracion con Jira (tablero GDICJ) -----
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
        if (estado == null) estado = EstadoIncidente.registrado;
        if (impacto == null) impacto = Nivel.medio;
        if (urgencia == null) urgencia = Nivel.medio;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
