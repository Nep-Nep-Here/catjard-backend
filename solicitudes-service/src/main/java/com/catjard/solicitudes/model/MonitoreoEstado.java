package com.catjard.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Estado persistente del monitoreo del host. Una sola fila (id = 1): guarda el ultimo
// arranque (boot time) conocido del Droplet, para detectar reinicios entre ciclos del
// scheduler. Vive en Postgres, asi que sobrevive a un reinicio del servidor (el volumen
// pgdata persiste) y permite comparar el arranque anterior con el nuevo.
@Entity
@Table(name = "monitoreo_estado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoreoEstado {

    @Id
    private Long id;                        // siempre 1 (singleton)

    @Column(name = "ultimo_arranque")
    private LocalDateTime ultimoArranque;   // boot time del host detectado

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
