package com.catjard.solicitudes.model;

// Etapas de Integracion Continua (PDF): Repositorio -> Build -> Testing -> Validacion -> Monitoreo.
// Aplica solo despues de que el cambio fue 'aprobado'. 'fallido' marca un pipeline con error.
public enum EtapaCI {
    pendiente, repositorio, build, testing, validacion, monitoreo, completado, fallido
}
