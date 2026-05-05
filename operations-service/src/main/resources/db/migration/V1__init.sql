CREATE TABLE artes (
    id BIGSERIAL PRIMARY KEY,
    pedido_codigo VARCHAR(20) NOT NULL,
    version INTEGER NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    comentarios_cliente TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_arte_estado CHECK (estado IN ('en_revision','aprobado','rechazado')),
    CONSTRAINT uk_arte_pedido_version UNIQUE (pedido_codigo, version)
);

CREATE INDEX idx_artes_pedido ON artes(pedido_codigo);

CREATE TABLE tracking_eventos (
    id BIGSERIAL PRIMARY KEY,
    pedido_codigo VARCHAR(20) NOT NULL,
    hito VARCHAR(40) NOT NULL,
    fecha DATE,
    completo BOOLEAN NOT NULL DEFAULT FALSE,
    observacion TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_track_hito CHECK (hito IN (
        'cotizacion_aprobada','en_diseno','arte_aprobado','en_produccion',
        'control_calidad','listo','despachado','entregado'
    )),
    CONSTRAINT uk_track_pedido_hito UNIQUE (pedido_codigo, hito)
);

CREATE INDEX idx_track_pedido ON tracking_eventos(pedido_codigo);

CREATE TABLE despachos (
    id BIGSERIAL PRIMARY KEY,
    pedido_codigo VARCHAR(20) NOT NULL UNIQUE,
    courier VARCHAR(60) NOT NULL,
    guia_remision VARCHAR(60),
    fecha_despacho DATE NOT NULL,
    fecha_entrega_real DATE,
    direccion_entrega VARCHAR(255),
    receptor VARCHAR(150),
    notas TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_despacho_courier CHECK (courier IN ('Olva Courier','Shalom'))
);
