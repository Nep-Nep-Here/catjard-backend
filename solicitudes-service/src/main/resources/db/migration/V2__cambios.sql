CREATE TABLE cambios (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    proyecto VARCHAR(160),
    version VARCHAR(10),
    tipo_cambio VARCHAR(15) NOT NULL,
    categoria VARCHAR(20) NOT NULL,
    titulo VARCHAR(160) NOT NULL,
    descripcion TEXT,
    impacto VARCHAR(10),
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    etapa_ci VARCHAR(15) NOT NULL,
    responsable VARCHAR(150),
    solicitante_email VARCHAR(150) NOT NULL,
    area_afectada VARCHAR(200),
    riesgo TEXT,
    nivel_riesgo VARCHAR(10),
    plan_pruebas TEXT,
    plan_rollback TEXT,
    jira_issue_key VARCHAR(40),
    jira_url VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cambio_tipo CHECK (tipo_cambio IN ('estandar','normal','emergencia')),
    CONSTRAINT chk_cambio_categoria CHECK (categoria IN ('infraestructura','aplicaciones','documentacion','calendario')),
    CONSTRAINT chk_cambio_prioridad CHECK (prioridad IN ('baja','media','alta')),
    CONSTRAINT chk_cambio_impacto CHECK (impacto IS NULL OR impacto IN ('bajo','medio','alto')),
    CONSTRAINT chk_cambio_nivel_riesgo CHECK (nivel_riesgo IS NULL OR nivel_riesgo IN ('bajo','medio','alto')),
    CONSTRAINT chk_cambio_estado CHECK (estado IN ('solicitado','en_evaluacion','aprobado','implementado','en_monitoreo','cerrado','rechazado')),
    CONSTRAINT chk_cambio_etapa_ci CHECK (etapa_ci IN ('pendiente','repositorio','build','testing','validacion','monitoreo','completado','fallido'))
);

CREATE INDEX idx_cambios_estado ON cambios(estado);
CREATE INDEX idx_cambios_categoria ON cambios(categoria);
CREATE INDEX idx_cambios_fecha ON cambios(fecha DESC);
