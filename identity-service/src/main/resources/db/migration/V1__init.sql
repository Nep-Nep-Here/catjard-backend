CREATE TABLE usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    rol                 VARCHAR(20)  NOT NULL,
    nombre              VARCHAR(255) NOT NULL,
    empresa             VARCHAR(255),
    ruc                 VARCHAR(11),
    telefono            VARCHAR(30),
    direccion           TEXT,
    cargo               VARCHAR(100),
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('cliente','vendedor','almacen','produccion','gerente'))
);

CREATE INDEX idx_usuarios_rol ON usuarios(rol);


/* se tomo con flayway  para la migracion */