package com.catjard.solicitudes.model;

// Como se detecto el incidente (fase de Identificacion).
// Canales realistas del stack Cat Jard (microservicios en un Droplet de DigitalOcean).
public enum OrigenIncidente {
    usuario,         // un usuario / cliente reporta el problema
    monitoreo,       // alertas / healthchecks del sistema
    mesa_ayuda,      // correo / telefono / formulario de soporte
    logs_servidor,   // logs del servidor (Droplet) o de los contenedores Docker
    pipeline_cicd,   // falla detectada en el pipeline de despliegue (CI/CD)
    equipo_dev       // lo detecta el equipo de desarrollo
}
