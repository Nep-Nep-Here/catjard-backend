package com.catjard.solicitudes.model;

// Flujo de control de cambios (PDF): Solicitar -> Evaluar -> Aprobar -> Implementar -> Monitorear.
// 'rechazado' es la salida cuando no se aprueba. Tras 'aprobado' arranca la Integracion Continua (etapaCI).
public enum EstadoCambio {
    solicitado, en_evaluacion, aprobado, implementado, en_monitoreo, cerrado, rechazado
}
