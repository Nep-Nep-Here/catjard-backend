package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Registro de una ejecucion de respaldo (Fase 5 - Plan de respaldos).
// Lo alimenta el cron del Droplet (via endpoint con token), el panel (manual)
// o el seeder (simulado). Con el ultimo respaldo exitoso por servicio se mide
// el RPO real contra el RPO objetivo del catalogo.
@Entity
@Table(name = "respaldos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;                 // RSP-2026-001

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;       // cuando se ejecuto el respaldo

    @Column(name = "servicio_id")
    private Long servicioId;               // servicio critico respaldado (opcional)

    @Column(nullable = false, length = 160)
    private String recurso;                // "6 BDs PostgreSQL (pg_dump)", "Droplet completo"...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoRespaldo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DestinoRespaldo destino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoRespaldo estado;

    @Column(name = "tamano_mb")
    private Double tamanoMb;

    @Column(name = "duracion_seg")
    private Integer duracionSeg;

    @Column(length = 255)
    private String mensaje;                // detalle o error del script

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrigenRespaldo origen;

    @Column(name = "externo_id", length = 60)
    private String externoId;              // id de la imagen en DigitalOcean (dedupe del sync)

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fechaHora == null) fechaHora = fechaCreacion;
    }
}
