-- Distingue a estudiantes de docentes y personal administrativo.
-- Los docentes no reportan programa academico ni semestre.
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS tipo_usuario VARCHAR(20) NOT NULL DEFAULT 'ESTUDIANTE';

-- Quien ya se registro con el correo docente queda marcado como tal.
UPDATE usuarios SET tipo_usuario = 'DOCENTE'
 WHERE correo LIKE '%@uniminuto.edu' AND tipo_usuario = 'ESTUDIANTE';
