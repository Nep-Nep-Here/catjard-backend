ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS cliente_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_usuarios_cliente_id ON usuarios(cliente_id);
