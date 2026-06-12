CREATE TABLE solicitudes (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    asunto VARCHAR(160) NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    solicitante_email VARCHAR(150) NOT NULL,
    solicitante_nombre VARCHAR(150),
    respuesta TEXT,
    atendido_por VARCHAR(150),
    jira_issue_key VARCHAR(40),
    jira_url VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sol_tipo CHECK (tipo IN ('servicio','informacion','acceso')),
    CONSTRAINT chk_sol_prioridad CHECK (prioridad IN ('baja','media','alta')),
    CONSTRAINT chk_sol_estado CHECK (estado IN ('nueva','en_evaluacion','aprobada','en_atencion','cerrada','rechazada'))
);

CREATE INDEX idx_sol_estado ON solicitudes(estado);
CREATE INDEX idx_sol_tipo ON solicitudes(tipo);
CREATE INDEX idx_sol_email ON solicitudes(solicitante_email);
CREATE INDEX idx_sol_fecha ON solicitudes(fecha DESC);
