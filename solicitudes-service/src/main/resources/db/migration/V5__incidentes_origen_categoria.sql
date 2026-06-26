-- Amplia los catalogos de origen y categoria de los incidentes con opciones
-- realistas del stack Cat Jard (microservicios + Docker en un Droplet).

ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS chk_inc_origen;
ALTER TABLE incidentes ADD CONSTRAINT chk_inc_origen
    CHECK (origen IN ('usuario','monitoreo','mesa_ayuda','logs_servidor','pipeline_cicd','equipo_dev'));

ALTER TABLE incidentes DROP CONSTRAINT IF EXISTS chk_inc_categoria;
ALTER TABLE incidentes ADD CONSTRAINT chk_inc_categoria
    CHECK (categoria IN ('infraestructura','aplicaciones','base_datos','redes','seguridad',
                         'despliegue','rendimiento','integracion','documentacion','otros'));
