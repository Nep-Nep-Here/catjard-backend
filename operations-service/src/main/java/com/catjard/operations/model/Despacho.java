package com.catjard.operations.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "despachos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_codigo", nullable = false, unique = true, length = 20)
    private String pedidoCodigo;

    @Column(nullable = false, length = 60)
    private String courier;

    @Column(name = "guia_remision", length = 60)
    private String guiaRemision;

    @Column(name = "fecha_despacho", nullable = false)
    private LocalDate fechaDespacho;

    @Column(name = "fecha_entrega_real")
    private LocalDate fechaEntregaReal;

    @Column(name = "direccion_entrega", length = 255)
    private String direccionEntrega;

    @Column(length = 150)
    private String receptor;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (fechaDespacho == null) fechaDespacho = LocalDate.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
