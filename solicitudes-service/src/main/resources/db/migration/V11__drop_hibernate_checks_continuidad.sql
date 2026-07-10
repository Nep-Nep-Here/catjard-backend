-- Mismo caso que V7: Hibernate (ddl-auto=update) creo sus propios CHECK de enum
-- en las tablas de continuidad cuando arranco con V9, y quedaron con las listas
-- de ese momento (p.ej. respaldos_origen_check sin 'digitalocean', agregado en
-- V10). Los CHECK de Flyway (chk_srv_*, chk_rsg_*, chk_rsp_*) siguen siendo la
-- fuente de verdad; los de Hibernate se eliminan.
ALTER TABLE respaldos DROP CONSTRAINT IF EXISTS respaldos_origen_check;
ALTER TABLE respaldos DROP CONSTRAINT IF EXISTS respaldos_tipo_check;
ALTER TABLE respaldos DROP CONSTRAINT IF EXISTS respaldos_destino_check;
ALTER TABLE respaldos DROP CONSTRAINT IF EXISTS respaldos_estado_check;

ALTER TABLE servicios_criticos DROP CONSTRAINT IF EXISTS servicios_criticos_tipo_check;
ALTER TABLE servicios_criticos DROP CONSTRAINT IF EXISTS servicios_criticos_criticidad_check;

ALTER TABLE riesgos DROP CONSTRAINT IF EXISTS riesgos_probabilidad_check;
ALTER TABLE riesgos DROP CONSTRAINT IF EXISTS riesgos_impacto_check;
ALTER TABLE riesgos DROP CONSTRAINT IF EXISTS riesgos_nivel_riesgo_check;
ALTER TABLE riesgos DROP CONSTRAINT IF EXISTS riesgos_estado_check;
