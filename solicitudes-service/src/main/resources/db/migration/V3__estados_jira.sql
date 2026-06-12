-- Alinear los estados de solicitudes con el workflow de Jira (3 estados).
-- Mapear los estados antiguos del flujo de 5/6 pasos a los nuevos.
UPDATE solicitudes SET estado = 'por_hacer'   WHERE estado IN ('nueva', 'en_evaluacion');
UPDATE solicitudes SET estado = 'en_curso'    WHERE estado IN ('aprobada', 'en_atencion');
UPDATE solicitudes SET estado = 'finalizado'  WHERE estado IN ('cerrada', 'rechazada');

-- Reemplazar la restriccion de estados.
ALTER TABLE solicitudes DROP CONSTRAINT IF EXISTS chk_sol_estado;
ALTER TABLE solicitudes ADD CONSTRAINT chk_sol_estado
    CHECK (estado IN ('por_hacer', 'en_curso', 'finalizado'));
