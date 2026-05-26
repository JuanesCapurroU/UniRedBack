-- Cerrar solicitudes PENDIENTE duplicadas (conservar la mas reciente por par estudiante-mentor)
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY estudiante_id, mentor_id
               ORDER BY fecha_solicitud DESC, id DESC
           ) AS rn
    FROM solicitudes_mentoria
    WHERE estado = 'PENDIENTE'
)
UPDATE solicitudes_mentoria s
SET estado = 'RECHAZADA',
    fecha_respuesta = NOW()
FROM ranked r
WHERE s.id = r.id
  AND r.rn > 1;

-- Evitar nuevas solicitudes duplicadas abiertas entre el mismo par
CREATE UNIQUE INDEX IF NOT EXISTS idx_solicitud_mentoria_par_abierta
    ON solicitudes_mentoria (estudiante_id, mentor_id)
    WHERE estado IN ('PENDIENTE', 'CONFIRMADA', 'FINALIZADA');
