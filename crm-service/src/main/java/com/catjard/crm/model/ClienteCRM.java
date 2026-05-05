package com.catjard.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes_crm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteCRM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(length = 120)
    private String industria;

    @Column(name = "contacto_principal", length = 150)
    private String contactoPrincipal;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(name = "cuenta_activa", nullable = false)
    private Boolean cuentaActiva;

    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta;

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
        if (cuentaActiva == null) cuentaActiva = Boolean.TRUE;
        if (fechaAlta == null) fechaAlta = LocalDate.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
