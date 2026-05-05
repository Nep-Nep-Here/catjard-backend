package com.catjard.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    @Column(name = "proveedor_nombre", nullable = false, length = 200)
    private String proveedorNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOC estado;

    @Column(name = "fecha_esperada")
    private LocalDate fechaEsperada;

    @Column(name = "fecha_recepcion")
    private LocalDate fechaRecepcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 120)
    private String usuario;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrdenCompraItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (estado == null) estado = EstadoOC.borrador;
        if (fecha == null) fecha = LocalDate.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (igv == null) igv = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
