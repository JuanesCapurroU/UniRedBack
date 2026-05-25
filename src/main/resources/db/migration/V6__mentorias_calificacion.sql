-- V6: Campos de ciclo y calificacion de mentorias
ALTER TABLE solicitudes_mentoria
    ADD COLUMN IF NOT EXISTS fecha_inicio TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS fecha_finalizacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS calificacion_final INTEGER,
    ADD COLUMN IF NOT EXISTS comentario_calificacion TEXT;
