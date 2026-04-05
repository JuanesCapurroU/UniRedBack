CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    dtype VARCHAR(31) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(80) NOT NULL,
    primer_apellido VARCHAR(80) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(120) NOT NULL UNIQUE,
    programa_academico VARCHAR(120),
    semestre INTEGER,
    sede VARCHAR(80),
    password_hash VARCHAR(255) NOT NULL,
    foto_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    nivel_acceso VARCHAR(30),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_dtype ON usuarios(dtype);

CREATE TABLE sesiones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_jwt TEXT NOT NULL UNIQUE,
    refresh_token TEXT NOT NULL UNIQUE,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP WITH TIME ZONE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    dispositivo_info VARCHAR(255)
);

CREATE INDEX idx_sesiones_usuario ON sesiones(usuario_id);
CREATE INDEX idx_sesiones_activo ON sesiones(activo);

CREATE TABLE actividades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    lugar VARCHAR(200),
    descripcion TEXT,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    duracion_minutos INTEGER NOT NULL DEFAULT 60,
    categoria VARCHAR(30) NOT NULL,
    cupo_total INTEGER NOT NULL,
    cupo_disponible INTEGER NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    recordatorio_wa BOOLEAN NOT NULL DEFAULT FALSE,
    administrador_id BIGINT REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_actividades_fecha ON actividades(fecha_hora);
CREATE INDEX idx_actividades_categoria ON actividades(categoria);

CREATE TABLE inscripciones (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    actividad_id BIGINT NOT NULL REFERENCES actividades(id) ON DELETE CASCADE,
    fecha_inscripcion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_cancelacion TIMESTAMP WITH TIME ZONE,
    recordatorio_activo BOOLEAN NOT NULL DEFAULT FALSE,
    asistio BOOLEAN,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT uk_inscripcion UNIQUE (estudiante_id, actividad_id)
);

CREATE TABLE mentores (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    calificacion_promedio DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sesiones_completadas INTEGER NOT NULL DEFAULT 0,
    sesiones_activas INTEGER NOT NULL DEFAULT 0,
    disponibilidad VARCHAR(500),
    bio TEXT,
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE mentor_materias (
    mentor_id BIGINT NOT NULL REFERENCES mentores(id) ON DELETE CASCADE,
    materia VARCHAR(120) NOT NULL,
    PRIMARY KEY (mentor_id, materia)
);

CREATE TABLE solicitudes_mentoria (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES usuarios(id),
    mentor_id BIGINT NOT NULL REFERENCES mentores(id),
    porcentaje_compatibilidad DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    motivacion TEXT,
    numero_whatsapp VARCHAR(20),
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_respuesta TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_solicitudes_estudiante ON solicitudes_mentoria(estudiante_id);
CREATE INDEX idx_solicitudes_mentor ON solicitudes_mentoria(mentor_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes_mentoria(estado);

CREATE TABLE recordatorios (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    descripcion TEXT NOT NULL,
    fecha_vencimiento TIMESTAMP WITH TIME ZONE NOT NULL,
    prioridad VARCHAR(20) NOT NULL DEFAULT 'PROXIMO',
    recibir_ventanas BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_actividades BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_mentoria BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_rrss BOOLEAN NOT NULL DEFAULT TRUE,
    numero_whatsapp VARCHAR(20)
);

CREATE TABLE configuracion_notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    recibir_tramites BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_actividades BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_mentoria BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_rrss BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_bot BOOLEAN NOT NULL DEFAULT TRUE,
    modificado_mentoria BOOLEAN NOT NULL DEFAULT FALSE,
    numero_whatsapp VARCHAR(20),
    fcm_token VARCHAR(500)
);

CREATE TABLE notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    publicacion_id BIGINT,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    prioridad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    url_accion VARCHAR(500),
    es_activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_leida ON notificaciones(leida);

CREATE TABLE publicaciones_rrss (
    id BIGSERIAL PRIMARY KEY,
    red_social VARCHAR(30) NOT NULL,
    perfil_nombre VARCHAR(120),
    contenido_texto TEXT,
    imagen_url VARCHAR(500),
    url_publicacion VARCHAR(500),
    hashtags VARCHAR(500),
    likes INTEGER NOT NULL DEFAULT 0,
    comentarios INTEGER NOT NULL DEFAULT 0,
    fecha_publicacion TIMESTAMP WITH TIME ZONE,
    fecha_cache TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    sincronizada BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_rrss_red_social ON publicaciones_rrss(red_social);
CREATE INDEX idx_rrss_fecha ON publicaciones_rrss(fecha_publicacion DESC);
