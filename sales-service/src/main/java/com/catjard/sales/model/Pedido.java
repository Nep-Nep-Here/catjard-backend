package com.catjard.sales.model;

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
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "cotizacion_codigo", length = 20)
    private String cotizacionCodigo;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 200)
    private String empresa;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "voucher_url", length = 255)
    private String voucherUrl;

    @Column(name = "voucher_fecha")
    private LocalDate voucherFecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoPedido estado;

    @Column(length = 60)
    private String courier;

    @Column(name = "guia_remision", length = 60)
    private String guiaRemision;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (estado == null) estado = EstadoPedido.por_iniciar;
        if (fechaPedido == null) fechaPedido = LocalDate.now();
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (igv == null) igv = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
