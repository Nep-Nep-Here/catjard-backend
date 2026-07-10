-- ============================================================================
-- Limpieza post-despliegue (idempotente: se puede correr las veces que sea).
--
-- Contexto: Flyway NO corre en este proyecto (Spring Boot 4 lo requiere como
-- modulo aparte), asi que el esquema lo maneja Hibernate (ddl-auto=update).
-- Hibernate crea CHECK de enums al crear cada tabla y NUNCA los actualiza al
-- agregar valores a un enum -> inserts nuevos fallan con
-- "viola la restriccion check <tabla>_<columna>_check".
--
-- Este script elimina TODOS los CHECK de esas tablas (la validacion de enums
-- ya la hace la aplicacion al parsear). Ejecutar tras cada despliegue:
--
--   Local:   psql -U postgres -d catjard_solicitudes -f scripts/post-deploy-drop-enum-checks.sql
--   Droplet: docker exec -i catjard-postgres-1 psql -U postgres -d catjard_solicitudes \
--              < scripts/post-deploy-drop-enum-checks.sql
-- ============================================================================
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT conrelid::regclass AS tabla, conname
        FROM pg_constraint
        WHERE contype = 'c'
          AND conrelid::regclass::text IN
              ('solicitudes','cambios','incidentes','eventos',
               'servicios_criticos','riesgos','respaldos')
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', r.tabla, r.conname);
        RAISE NOTICE 'Eliminado: % en %', r.conname, r.tabla;
    END LOOP;
END $$;
