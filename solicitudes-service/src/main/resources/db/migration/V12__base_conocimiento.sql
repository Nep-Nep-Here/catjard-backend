-- Base de Conocimiento: planes de continuidad, DRP, respaldos, politicas y
-- runbooks (estrategia de recuperacion paso a paso por escenario).
-- Un articulo puede vincularse a una categoria de incidente y/o a un servicio
-- del catalogo: el detalle del incidente sugiere la estrategia documentada y la
-- referencia viaja en el issue de Jira.
--
-- NOTA: Flyway no corre en este proyecto (ver V11); Hibernate crea esta tabla
-- al arrancar. Este archivo queda como registro del esquema.
CREATE TABLE articulos_kb (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(160) NOT NULL,
    categoria VARCHAR(25) NOT NULL,
    resumen VARCHAR(400),
    contenido TEXT NOT NULL,
    autor VARCHAR(150),
    vistas INT NOT NULL DEFAULT 0,
    categoria_incidente VARCHAR(20),
    servicio_id BIGINT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kb_categoria ON articulos_kb(categoria);
CREATE INDEX idx_kb_cat_incidente ON articulos_kb(categoria_incidente);
CREATE INDEX idx_kb_servicio ON articulos_kb(servicio_id);
