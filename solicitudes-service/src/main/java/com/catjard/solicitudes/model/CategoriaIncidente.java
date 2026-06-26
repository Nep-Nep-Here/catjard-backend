package com.catjard.solicitudes.model;

// Clasificacion del incidente. Tipos del docente + categorias propias del stack
// Cat Jard (microservicios + Docker en un Droplet de DigitalOcean).
public enum CategoriaIncidente {
    infraestructura,  // servidor / Droplet / Docker
    aplicaciones,     // los microservicios
    base_datos,       // PostgreSQL
    redes,            // redes y comunicaciones / conectividad
    seguridad,        // accesos no autorizados, vulnerabilidades
    despliegue,       // despliegue / DevOps (Docker, CI/CD)
    rendimiento,      // lentitud / saturacion de recursos / disponibilidad
    integracion,      // fallo de integracion entre servicios (Eureka, API Gateway)
    documentacion,    // manuales / procedimientos desactualizados
    otros             // lo que no encaja en las anteriores
}
