-- V5: Sistema de verificación de correo
CREATE TABLE codigo_verificacion (
    id BIGSERIAL PRIMARY KEY,
    correo VARCHAR(120) NOT NULL,
    codigo VARCHAR(6) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    usado BOOLEAN DEFAULT FALSE,
    intentos INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_codigo_verificacion_correo ON codigo_verificacion(correo);
CREATE INDEX idx_codigo_verificacion_expires ON codigo_verificacion(expires_at);

ALTER TABLE usuarios ADD COLUMN verificado BOOLEAN DEFAULT FALSE;