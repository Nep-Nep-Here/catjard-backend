package com.catjard.operations.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_codigo", nullable = false, length = 20)
    private String pedidoCodigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private HitoTracking hito;

    private LocalDate fecha;

    @Column(nullable = false)
    private Boolean completo;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (completo == null) completo = Boolean.FALSE;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
