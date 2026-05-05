CREATE TABLE cotizaciones (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    empresa VARCHAR(200) NOT NULL,
    ruc VARCHAR(11),
    fecha DATE NOT NULL,
    logo_nombre VARCHAR(255),
    notas_cliente TEXT,
    estado VARCHAR(20) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    validez DATE,
    notas_vendedor TEXT,
    vendedor VARCHAR(120),
    motivo_rechazo TEXT,
    pedido_codigo VARCHAR(20),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cot_estado CHECK (estado IN ('enviada','en_revision','propuesta','aprobada','rechazada'))
);

CREATE INDEX idx_cot_estado ON cotizaciones(estado);
CREATE INDEX idx_cot_cliente ON cotizaciones(cliente_id);
CREATE INDEX idx_cot_fecha ON cotizaciones(fecha DESC);

CREATE TABLE cotizacion_items (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unit NUMERIC(10, 2) NOT NULL,
    tecnica VARCHAR(60),
    notas TEXT
);

CREATE INDEX idx_cot_items_cot ON cotizacion_items(cotizacion_id);

CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    cotizacion_codigo VARCHAR(20),
    cliente_id BIGINT NOT NULL,
    empresa VARCHAR(200) NOT NULL,
    fecha_pedido DATE NOT NULL,
    fecha_entrega_estimada DATE,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    voucher_url VARCHAR(255),
    voucher_fecha DATE,
    estado VARCHAR(40) NOT NULL,
    courier VARCHAR(60),
    guia_remision VARCHAR(60),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ped_estado CHECK (estado IN (
        'por_iniciar','en_diseno','esperando_aprobacion_arte','en_produccion',
        'control_calidad','listo','despachado','entregado'
    ))
);

CREATE INDEX idx_ped_estado ON pedidos(estado);
CREATE INDEX idx_ped_cliente ON pedidos(cliente_id);
CREATE INDEX idx_ped_cot ON pedidos(cotizacion_codigo);

CREATE TABLE pedido_items (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unit NUMERIC(10, 2) NOT NULL,
    tecnica VARCHAR(60)
);

CREATE INDEX idx_ped_items_ped ON pedido_items(pedido_id);
