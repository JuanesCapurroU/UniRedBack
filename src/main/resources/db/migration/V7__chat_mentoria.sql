-- V7: Chat interno por solicitud de mentoria
CREATE TABLE IF NOT EXISTS mensajes_mentoria (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes_mentoria(id) ON DELETE CASCADE,
    emisor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contenido TEXT NOT NULL,
    fecha_envio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mensajes_mentoria_solicitud ON mensajes_mentoria(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_mensajes_mentoria_fecha ON mensajes_mentoria(fecha_envio);
