-- Gestion de Incidentes: registro, clasificacion, priorizacion (matriz Impacto x Urgencia),
-- diagnostico, resolucion y cierre. Integracion con el tablero GDICJ en Jira.
CREATE TABLE incidentes (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    titulo VARCHAR(160) NOT NULL,
    descripcion TEXT,
    origen VARCHAR(15) NOT NULL,
    servicio_afectado VARCHAR(200),
    categoria VARCHAR(20) NOT NULL,
    impacto VARCHAR(10) NOT NULL,
    urgencia VARCHAR(10) NOT NULL,
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    responsable VARCHAR(150),
    solicitante_email VARCHAR(150) NOT NULL,
    diagnostico TEXT,
    solucion TEXT,
    evidencia TEXT,
    fecha_resolucion TIMESTAMP,
    fecha_cierre TIMESTAMP,
    jira_issue_key VARCHAR(40),
    jira_url VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_inc_origen CHECK (origen IN ('usuario','monitoreo','mesa_ayuda')),
    CONSTRAINT chk_inc_categoria CHECK (categoria IN ('infraestructura','aplicaciones','base_datos','redes','seguridad','documentacion','otros')),
    CONSTRAINT chk_inc_impacto CHECK (impacto IN ('bajo','medio','alto')),
    CONSTRAINT chk_inc_urgencia CHECK (urgencia IN ('bajo','medio','alto')),
    CONSTRAINT chk_inc_prioridad CHECK (prioridad IN ('baja','media','alta','critica')),
    CONSTRAINT chk_inc_estado CHECK (estado IN ('registrado','en_diagnostico','en_resolucion','resuelto','cerrado','reabierto','cancelado'))
);

CREATE INDEX idx_incidentes_estado ON incidentes(estado);
CREATE INDEX idx_incidentes_categoria ON incidentes(categoria);
CREATE INDEX idx_incidentes_prioridad ON incidentes(prioridad);
CREATE INDEX idx_incidentes_fecha ON incidentes(fecha DESC);
