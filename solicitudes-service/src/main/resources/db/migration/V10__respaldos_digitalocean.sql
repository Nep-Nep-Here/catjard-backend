-- Sincronizacion de respaldos con DigitalOcean: los backups automaticos y
-- snapshots del Droplet (API /v2/droplets/{id}/backups|snapshots) se registran
-- en la tabla respaldos. 'externo_id' es el id de la imagen en DO (dedupe).
ALTER TABLE respaldos ADD COLUMN externo_id VARCHAR(60);
CREATE UNIQUE INDEX idx_respaldos_externo ON respaldos(externo_id) WHERE externo_id IS NOT NULL;

-- Nuevo origen 'digitalocean' (imagen tomada por DO, leida via API).
ALTER TABLE respaldos DROP CONSTRAINT chk_rsp_origen;
ALTER TABLE respaldos ADD CONSTRAINT chk_rsp_origen
    CHECK (origen IN ('script','manual','simulado','digitalocean'));
