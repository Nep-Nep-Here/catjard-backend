package com.catjard.solicitudes.dto;

import java.time.LocalDateTime;
import java.util.List;

// Tablero resumen del plan de continuidad: cumplimiento RTO medido sobre los
// incidentes con servicio asociado y estado RPO por servicio (ultimo respaldo
// exitoso vs. RPO objetivo). Es la tabla de "Resultados esperados" en vivo.
public record ResumenContinuidadDTO(
        long totalServicios,
        long serviciosCriticidadAlta,        // criticidad alta + critica
        long riesgosAbiertos,                // identificado + en_mitigacion
        long incidentesConRto,               // incidentes historicos con deadline RTO
        long incidentesRtoCumplido,
        long incidentesRtoIncumplido,
        Double porcentajeCumplimientoRto,    // null si aun no hay incidentes medidos
        long incidentesActivosRtoVencido,    // abiertos que ya pasaron su deadline
        List<EstadoRpoDTO> estadoRpo
) {
    // Semaforo RPO de un servicio con datos propios (rpoMinutos definido).
    public record EstadoRpoDTO(
            Long servicioId,
            String servicioNombre,
            Integer rpoObjetivoMinutos,
            LocalDateTime ultimoRespaldo,
            Long minutosDesdeUltimoRespaldo, // null = nunca se respaldo
            boolean cumple
    ) {}
}
