CREATE TABLE productos (
    id                  BIGSERIAL PRIMARY KEY,
    slug                VARCHAR(100) NOT NULL UNIQUE,
    nombre              VARCHAR(255) NOT NULL,
    categoria           VARCHAR(20)  NOT NULL,
    precio              DECIMAL(10, 2) NOT NULL,
    stock               INTEGER      NOT NULL DEFAULT 0,
    stock_minimo        INTEGER      NOT NULL DEFAULT 50,
    descripcion         TEXT,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_productos_categoria CHECK (categoria IN ('vestimenta','oficina','bebidas','tecnologia','bolsos')),
    CONSTRAINT chk_productos_precio    CHECK (precio >= 0),
    CONSTRAINT chk_productos_stock     CHECK (stock >= 0)
);
CREATE INDEX idx_productos_categoria ON productos(categoria);

CREATE TABLE producto_tecnicas (
    producto_id BIGINT      NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    tecnica     VARCHAR(30) NOT NULL,
    PRIMARY KEY (producto_id, tecnica)
);

CREATE TABLE promociones (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255)  NOT NULL,
    descripcion         TEXT,
    descuento_pct       DECIMAL(5, 2) NOT NULL,
    desde               DATE          NOT NULL,
    hasta               DATE          NOT NULL,
    aplica_a            VARCHAR(20)   NOT NULL,
    categoria_aplica    VARCHAR(20),
    producto_id         BIGINT,
    activa              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_promociones_aplica_a CHECK (aplica_a IN ('todo','categoria','producto')),
    CONSTRAINT chk_promociones_descuento CHECK (descuento_pct >= 0 AND descuento_pct <= 100),
    CONSTRAINT chk_promociones_fechas    CHECK (hasta >= desde),
    CONSTRAINT chk_promociones_polimorfismo CHECK (
        (aplica_a = 'todo'      AND categoria_aplica IS NULL     AND producto_id IS NULL) OR
        (aplica_a = 'categoria' AND categoria_aplica IS NOT NULL AND producto_id IS NULL) OR
        (aplica_a = 'producto'  AND categoria_aplica IS NULL     AND producto_id IS NOT NULL)
    )
);
CREATE INDEX idx_promociones_activa ON promociones(activa);
