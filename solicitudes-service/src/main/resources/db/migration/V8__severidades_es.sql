-- Renombra las severidades de los eventos al espanol:
-- warning -> advertencia, error -> alto, critical -> critico (informacion no cambia).
-- Primero se sueltan los CHECK viejos (el de Flyway y el de Hibernate) para poder actualizar.
ALTER TABLE eventos DROP CONSTRAINT IF EXISTS chk_evt_severidad;
ALTER TABLE eventos DROP CONSTRAINT IF EXISTS eventos_severidad_check;

UPDATE eventos SET severidad = 'advertencia' WHERE severidad = 'warning';
UPDATE eventos SET severidad = 'alto'        WHERE severidad = 'error';
UPDATE eventos SET severidad = 'critico'     WHERE severidad = 'critical';

ALTER TABLE eventos ADD CONSTRAINT chk_evt_severidad
    CHECK (severidad IN ('informacion','advertencia','alto','critico'));
