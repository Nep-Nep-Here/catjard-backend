package com.catjard.crm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String empresa;

    @Column(length = 11)
    private String ruc;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String productos;

    @Column(length = 50)
    private String cantidad;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLead estado;

    @Column(name = "asignado_a", length = 120)
    private String asignadoA;

    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        var now = LocalDateTime.now();
        fechaCreacion = now;
        fechaActualizacion = now;
        if (estado == null) estado = EstadoLead.nuevo;
        if (fecha == null) fecha = LocalDate.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
