package com.catjard.solicitudes.model;

// Ciclo de vida simple de un evento de monitoreo en el panel.
public enum EstadoEvento {
    nuevo,        // recien detectado por el scheduler
    en_revision,  // alguien lo esta mirando
    atendido,     // se ejecuto la accion del plan de respuesta
    descartado    // falso positivo / ruido
}
