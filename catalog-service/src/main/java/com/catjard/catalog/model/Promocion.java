package com.catjard.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promociones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "descuento_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoPct;

    @Column(nullable = false)
    private LocalDate desde;

    @Column(nullable = false)
    private LocalDate hasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplica_a", nullable = false, length = 20)
    private AplicaA aplicaA;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_aplica", length = 20)
    private Categoria categoriaAplica;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        this.fechaCreacion = now;
        this.fechaActualizacion = now;
    }

    @PreUpdate
    void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
