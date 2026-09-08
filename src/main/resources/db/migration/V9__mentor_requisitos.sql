-- Requisitos declarados por el estudiante al postularse como mentor (el admin los verifica)
ALTER TABLE mentores ADD COLUMN IF NOT EXISTS promedio_academico DOUBLE PRECISION;
ALTER TABLE mentores ADD COLUMN IF NOT EXISTS sin_materias_perdidas BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mentores ADD COLUMN IF NOT EXISTS sin_procesos_disciplinarios BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mentores ADD COLUMN IF NOT EXISTS horas_semanales INTEGER;
