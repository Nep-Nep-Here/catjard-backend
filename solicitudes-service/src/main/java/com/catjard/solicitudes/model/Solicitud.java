package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitud tipo;

    @Column(nullable = false, length = 160)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "solicitante_email", nullable = false, length = 150)
    private String solicitanteEmail;

    @Column(name = "solicitante_nombre", length = 150)
    private String solicitanteNombre;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "atendido_por", length = 150)
    private String atendidoPor;

    // Trazabilidad con Jira: clave del issue (ej. SUP-123) y enlace navegable.
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
        if (estado == null) estado = EstadoSolicitud.por_hacer;
        if (prioridad == null) prioridad = Prioridad.media;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
