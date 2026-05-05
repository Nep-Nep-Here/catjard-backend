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
@Table(name = "cotizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 200)
    private String empresa;

    @Column(length = 11)
    private String ruc;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "logo_nombre", length = 255)
    private String logoNombre;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    private LocalDate validez;

    @Column(name = "notas_vendedor", columnDefinition = "TEXT")
    private String notasVendedor;

    @Column(length = 120)
    private String vendedor;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "pedido_codigo", length = 20)
    private String pedidoCodigo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<CotizacionItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (estado == null) estado = EstadoCotizacion.enviada;
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
