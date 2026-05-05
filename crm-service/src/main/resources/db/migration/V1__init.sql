CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    empresa VARCHAR(150),
    ruc VARCHAR(11),
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    productos TEXT,
    cantidad VARCHAR(50),
    mensaje TEXT,
    estado VARCHAR(20) NOT NULL,
    asignado_a VARCHAR(120),
    notas_internas TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_lead_estado CHECK (estado IN ('nuevo','contactado','convertido','descartado'))
);

CREATE INDEX idx_leads_estado ON leads(estado);
CREATE INDEX idx_leads_fecha ON leads(fecha DESC);

CREATE TABLE clientes_crm (
    id BIGSERIAL PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    ruc VARCHAR(11) NOT NULL UNIQUE,
    industria VARCHAR(120),
    contacto_principal VARCHAR(150),
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    cuenta_activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATE NOT NULL,
    notas TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clientes_ruc ON clientes_crm(ruc);
CREATE INDEX idx_clientes_activa ON clientes_crm(cuenta_activa);
