package com.catjard.solicitudes.dto;

import jakarta.validation.constraints.Size;

// El gerente evalua/aprueba/rechaza y mueve el flujo y la etapa de CI.
// Todos los campos son opcionales: se aplica solo lo que venga.
public record ActualizarCambioDTO(
        @Size(max = 20) String estado,        // en_evaluacion / aprobado / implementado / en_monitoreo / cerrado / rechazado
        @Size(max = 15) String etapaCI,       // repositorio / build / testing / validacion / monitoreo / completado / fallido
        @Size(max = 10) String prioridad,
        @Size(max = 10) String impacto,
        @Size(max = 10) String nivelRiesgo,
        @Size(max = 150) String responsable,
        @Size(max = 200) String areaAfectada,
        String riesgo,
        String planPruebas,
        String planRollback
) {}
