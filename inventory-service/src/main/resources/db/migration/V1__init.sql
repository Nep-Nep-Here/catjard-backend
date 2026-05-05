CREATE TABLE proveedores (
    id BIGSERIAL PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    ruc VARCHAR(11) NOT NULL UNIQUE,
    contacto VARCHAR(150),
    email VARCHAR(150),
    telefono VARCHAR(30),
    direccion VARCHAR(255),
    productos TEXT,
    notas TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATE NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prov_activo ON proveedores(activo);

CREATE TABLE ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha DATE NOT NULL,
    proveedor_id BIGINT NOT NULL REFERENCES proveedores(id),
    proveedor_nombre VARCHAR(200) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_esperada DATE,
    fecha_recepcion DATE,
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    usuario VARCHAR(120),
    notas TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_oc_estado CHECK (estado IN ('borrador','enviada','recibida','cancelada'))
);

CREATE INDEX idx_oc_estado ON ordenes_compra(estado);
CREATE INDEX idx_oc_proveedor ON ordenes_compra(proveedor_id);

CREATE TABLE orden_compra_items (
    id BIGSERIAL PRIMARY KEY,
    orden_compra_id BIGINT NOT NULL REFERENCES ordenes_compra(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unit NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_oc_items_oc ON orden_compra_items(orden_compra_id);

CREATE TABLE movimientos (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL,
    motivo VARCHAR(120) NOT NULL,
    referencia VARCHAR(40),
    usuario VARCHAR(120),
    notas TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mov_tipo CHECK (tipo IN ('entrada','salida','ajuste'))
);

CREATE INDEX idx_mov_producto ON movimientos(producto_id);
CREATE INDEX idx_mov_fecha ON movimientos(fecha DESC);
CREATE INDEX idx_mov_tipo ON movimientos(tipo);
