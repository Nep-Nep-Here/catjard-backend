-- Hibernate (ddl-auto=update) creo la tabla incidentes con sus propios CHECK de enum,
-- que quedaron con las listas antiguas (sin 'rendimiento', 'despliegue', 'integracion'...).
-- V5 solo actualizo los CHECK de Flyway (chk_inc_*), que siguen siendo la fuente de verdad.
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_categoria_check;
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_origen_check;
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_estado_check;
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_prioridad_check;
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_impacto_check;
ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS incidentes_urgencia_check;
