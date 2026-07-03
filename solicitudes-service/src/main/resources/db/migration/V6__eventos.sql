-- Monitoreo Estrategico y Gestion de Eventos: eventos detectados en el Droplet
-- (API de Monitoring de DigitalOcean) o simulados. Clasificacion por severidad,
-- identificacion (¿genera incidente?) y plan de respuesta autodesignado.
CREATE TABLE eventos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha_hora TIMESTAMP NOT NULL,
    origen VARCHAR(20) NOT NULL,
    droplet VARCHAR(100),
    metrica VARCHAR(15) NOT NULL,
    valor DOUBLE PRECISION NOT NULL,
    unidad VARCHAR(10) NOT NULL,
    umbral DOUBLE PRECISION,
    severidad VARCHAR(15) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    genera_incidente BOOLEAN NOT NULL DEFAULT FALSE,
    justificacion TEXT,
    incidente_id BIGINT,
    incidente_codigo VARCHAR(20),
    accion_recomendada TEXT,
    responsable VARCHAR(150),
    tiempo_maximo VARCHAR(30),
    jira_issue_key VARCHAR(40),
    jira_url VARCHAR(255),
    estado VARCHAR(15) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_evt_origen CHECK (origen IN ('digitalocean','simulado')),
    CONSTRAINT chk_evt_metrica CHECK (metrica IN ('cpu','memoria','disco','load','red_entrada','red_salida')),
    CONSTRAINT chk_evt_severidad CHECK (severidad IN ('informacion','warning','error','critical')),
    CONSTRAINT chk_evt_estado CHECK (estado IN ('nuevo','en_revision','atendido','descartado')),
    CONSTRAINT fk_evt_incidente FOREIGN KEY (incidente_id) REFERENCES incidentes(id) ON DELETE SET NULL
);

CREATE INDEX idx_eventos_severidad ON eventos(severidad);
CREATE INDEX idx_eventos_estado ON eventos(estado);
CREATE INDEX idx_eventos_metrica ON eventos(metrica);
CREATE INDEX idx_eventos_fecha ON eventos(fecha_hora DESC);
