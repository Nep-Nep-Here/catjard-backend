-- Gestion de Continuidad del Servicio y Recuperacion ante Desastres (DRP).
-- Fase 1/3: catalogo de servicios criticos con RTO/RPO objetivo.
-- Fase 2:   matriz de riesgos vinculada a los servicios afectados.
-- Fase 5:   registro de ejecuciones de respaldo (trazabilidad de la regla 3-2-1).
-- Ademas: contador RTO en incidentes (deadline y cumplimiento medido).

CREATE TABLE servicios_criticos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(20) NOT NULL,
    criticidad VARCHAR(10) NOT NULL,
    prioridad_recuperacion INT NOT NULL DEFAULT 99,
    rto_minutos INT,
    rpo_minutos INT,
    estrategia_continuidad TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_srv_tipo CHECK (tipo IN ('microservicio','base_datos','frontend','infraestructura')),
    CONSTRAINT chk_srv_criticidad CHECK (criticidad IN ('baja','media','alta','critica'))
);

CREATE TABLE riesgos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(160) NOT NULL,
    descripcion TEXT,
    probabilidad VARCHAR(10) NOT NULL,
    impacto VARCHAR(10) NOT NULL,
    nivel_riesgo VARCHAR(10) NOT NULL,
    accion_mitigacion TEXT,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rsg_probabilidad CHECK (probabilidad IN ('bajo','medio','alto')),
    CONSTRAINT chk_rsg_impacto CHECK (impacto IN ('bajo','medio','alto')),
    CONSTRAINT chk_rsg_nivel CHECK (nivel_riesgo IN ('bajo','medio','alto','critico')),
    CONSTRAINT chk_rsg_estado CHECK (estado IN ('identificado','en_mitigacion','mitigado','aceptado'))
);

-- Relacion N:M riesgo <-> servicios afectados.
CREATE TABLE riesgo_servicios (
    riesgo_id BIGINT NOT NULL REFERENCES riesgos(id) ON DELETE CASCADE,
    servicio_id BIGINT NOT NULL REFERENCES servicios_criticos(id) ON DELETE CASCADE,
    PRIMARY KEY (riesgo_id, servicio_id)
);

CREATE TABLE respaldos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha_hora TIMESTAMP NOT NULL,
    servicio_id BIGINT REFERENCES servicios_criticos(id) ON DELETE SET NULL,
    recurso VARCHAR(160) NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    estado VARCHAR(10) NOT NULL,
    tamano_mb DOUBLE PRECISION,
    duracion_seg INT,
    mensaje VARCHAR(255),
    origen VARCHAR(15) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rsp_tipo CHECK (tipo IN ('completo','incremental','snapshot')),
    CONSTRAINT chk_rsp_destino CHECK (destino IN ('droplet_local','snapshot_do','copia_externa')),
    CONSTRAINT chk_rsp_estado CHECK (estado IN ('exitoso','fallido')),
    CONSTRAINT chk_rsp_origen CHECK (origen IN ('script','manual','simulado'))
);

CREATE INDEX idx_respaldos_servicio ON respaldos(servicio_id, fecha_hora DESC);
CREATE INDEX idx_respaldos_fecha ON respaldos(fecha_hora DESC);
CREATE INDEX idx_riesgos_nivel ON riesgos(nivel_riesgo);

-- Contador RTO por incidente: al asociarlo a un servicio critico se fija el
-- deadline (creacion + RTO objetivo); al resolver se mide si se cumplio.
ALTER TABLE incidentes ADD COLUMN servicio_id BIGINT REFERENCES servicios_criticos(id) ON DELETE SET NULL;
ALTER TABLE incidentes ADD COLUMN servicio_nombre VARCHAR(120);
ALTER TABLE incidentes ADD COLUMN rto_minutos INT;
ALTER TABLE incidentes ADD COLUMN rto_deadline TIMESTAMP;
ALTER TABLE incidentes ADD COLUMN cumplio_rto BOOLEAN;
