package com.catjard.solicitudes.model;

// Fase 1 - Clasificacion de Eventos: severidad segun el umbral que cruza la metrica.
// informacion = operacion normal / recuperacion; advertencia = observacion (proactivo);
// alto / critico = degradacion o riesgo de indisponibilidad -> generan incidente.
public enum SeveridadEvento {
    informacion,
    advertencia,
    alto,
    critico
}
