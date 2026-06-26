package com.catjard.solicitudes.model;

// Ciclo de vida operativo del incidente (mesa de ayuda / sistemas).
// Las etapas del docente Identificacion/Registro/Clasificacion/Priorizacion ocurren al
// REGISTRAR el ticket; Diagnostico/Resolucion/Validacion/Cierre son los estados que avanzan.
// 'reabierto' vuelve un incidente cerrado a atencion; 'cancelado' lo descarta (falso positivo).
public enum EstadoIncidente {
    registrado, en_diagnostico, en_resolucion, resuelto, cerrado, reabierto, cancelado
}
