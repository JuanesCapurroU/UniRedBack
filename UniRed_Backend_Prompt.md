# 🎓 UniRed — Prompt de Implementación Backend (Spring Boot)

> **Proyecto:** UniRed — Plataforma Estudiantil Integral · Universidad Minuto de Dios  
> **Stack backend:** Spring Boot (Spring Initializr) · PostgreSQL · JWT · FCM  
> **Audiencia de este prompt:** Codex / agente de codificación automática  
> **Objetivo:** Genera el backend completo, production-ready, siguiendo exactamente las instrucciones de cada sección.

---

## 1. Contexto general del proyecto

UniRed es el backend REST de una aplicación Android nativa para la Universidad Minuto de Dios (sede Zipaquirá). Sirve a estudiantes y administradores. Expone APIs consumidas por la app móvil para:

- Autenticación con correo institucional y JWT
- Dashboard personal del estudiante
- Centro de comunicaciones (feed de redes sociales institucionales)
- Agenda de actividades con inscripción en tiempo real
- Sistema de mentorías académicas con algoritmo de compatibilidad
- Notificaciones push vía Firebase Cloud Messaging (FCM)
- Gestión de perfil académico

El cliente móvil es Android (Kotlin + Retrofit). El backend **nunca** gestiona videollamadas ni publica en redes sociales; solo consulta y sirve datos.

---

## 2. Stack tecnológico obligatorio

| Capa | Tecnología |
|---|---|
| Framework | Spring Boot 3.x (Spring Initializr) |
| Lenguaje | Java 21 (LTS) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL 16 |
| Driver JDBC | PostgreSQL Driver (org.postgresql) |
| Reducción boilerplate | Lombok |
| Validación | Spring Validation (jakarta.validation) |
| Desarrollo | Spring DevTools |
| Seguridad | Spring Security + JWT (jjwt 0.12.x) |
| Notificaciones push | Firebase Admin SDK |
| Hash de contraseñas | BCryptPasswordEncoder (factor 12) |
| Migraciones DB | Flyway |
| Documentación API | SpringDoc OpenAPI 3 (Swagger UI) |
| Mapeo de objetos | MapStruct |

### `pom.xml` — dependencias que el agente DEBE incluir

```xml
<!-- Spring Boot Starters -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-devtools</artifactId>
  <scope>runtime</scope>
  <optional>true</optional>
</dependency>

<!-- PostgreSQL -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <optional>true</optional>
</dependency>

<!-- JWT -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.3</version>
  <scope>runtime</scope>
</dependency>

<!-- Firebase Admin -->
<dependency>
  <groupId>com.google.firebase</groupId>
  <artifactId>firebase-admin</artifactId>
  <version>9.2.0</version>
</dependency>

<!-- Flyway -->
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>

<!-- SpringDoc OpenAPI -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>

<!-- MapStruct -->
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct</artifactId>
  <version>1.5.5.Final</version>
</dependency>
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct-processor</artifactId>
  <version>1.5.5.Final</version>
  <scope>provided</scope>
</dependency>
```

---

## 3. Estructura de paquetes obligatoria

```
com.unired
├── UniRedApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── FirebaseConfig.java
│   ├── OpenApiConfig.java
│   └── WebConfig.java (CORS)
├── domain/
│   ├── model/           ← Entidades JPA (@Entity)
│   ├── enums/           ← Enumeraciones del dominio
│   └── repository/      ← Interfaces JPA Repository
├── application/
│   ├── service/         ← Lógica de negocio (@Service)
│   ├── dto/
│   │   ├── request/     ← DTOs de entrada
│   │   └── response/    ← DTOs de salida
│   └── mapper/          ← MapStruct mappers
├── infrastructure/
│   ├── security/        ← JwtFilter, JwtUtil, UserDetailsServiceImpl
│   ├── firebase/        ← FcmService
│   └── external/        ← Adaptadores redes sociales
├── api/
│   └── controller/      ← @RestController por módulo
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── custom/          ← Excepciones de negocio
└── util/
    └── constants/
```

---

## 4. Variables de entorno y `application.yml`

El agente DEBE crear el siguiente `application.yml`. **Nunca hardcodear secretos.**

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  application:
    name: unired-backend
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/unired_db}
    username: ${DB_USER:unired}
    password: ${DB_PASSWORD:secret}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: validate          # Flyway gestiona el esquema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        default_schema: public
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET}           # mínimo 512 bits en Base64
  expiration-ms: 1800000          # 30 minutos
  refresh-expiration-ms: 604800000 # 7 días

firebase:
  credentials-path: ${FIREBASE_CREDENTIALS_PATH:classpath:firebase-service-account.json}

logging:
  level:
    com.unired: DEBUG
    org.springframework.security: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 5. Esquema de base de datos (Flyway)

### Instrucción al agente

Crea los scripts en `src/main/resources/db/migration/` con la convención `V{n}__{descripcion}.sql`.

### V1__create_core_tables.sql

```sql
-- USUARIOS (tabla base, herencia single-table con dtype)
CREATE TABLE usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    dtype               VARCHAR(31) NOT NULL,          -- 'Estudiante' | 'Administrador'
    tipo_documento      VARCHAR(20) NOT NULL,
    numero_documento    VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre       VARCHAR(80) NOT NULL,
    primer_apellido     VARCHAR(80) NOT NULL,
    telefono            VARCHAR(20),
    correo              VARCHAR(120) NOT NULL UNIQUE,
    programa_academico  VARCHAR(120),
    semestre            INTEGER,
    sede                VARCHAR(80),
    password_hash       VARCHAR(255) NOT NULL,
    foto_url            VARCHAR(500),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    nivel_acceso        VARCHAR(30),                    -- solo Administrador
    fecha_creacion      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_dtype  ON usuarios(dtype);

-- SESIONES (control de tokens activos + refresh)
CREATE TABLE sesiones (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_jwt       TEXT NOT NULL UNIQUE,
    refresh_token   TEXT NOT NULL UNIQUE,
    fecha_inicio    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP WITH TIME ZONE NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    dispositivo_info VARCHAR(255)
);

CREATE INDEX idx_sesiones_usuario  ON sesiones(usuario_id);
CREATE INDEX idx_sesiones_activo   ON sesiones(activo);

-- ACTIVIDADES
CREATE TABLE actividades (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    lugar           VARCHAR(200),
    descripcion     TEXT,
    fecha_hora      TIMESTAMP WITH TIME ZONE NOT NULL,
    duracion_minutos INTEGER NOT NULL DEFAULT 60,
    categoria       VARCHAR(30) NOT NULL,               -- ENUM: CULTURAL, DEPORTES, APOYO, SALUD
    cupo_total      INTEGER NOT NULL,
    cupo_disponible INTEGER NOT NULL,
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    recordatorio_wa BOOLEAN NOT NULL DEFAULT FALSE,
    administrador_id BIGINT REFERENCES usuarios(id),
    fecha_creacion  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_actividades_fecha     ON actividades(fecha_hora);
CREATE INDEX idx_actividades_categoria ON actividades(categoria);

-- INSCRIPCIONES (Estudiante ↔ Actividad)
CREATE TABLE inscripciones (
    id                  BIGSERIAL PRIMARY KEY,
    estudiante_id       BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    actividad_id        BIGINT NOT NULL REFERENCES actividades(id) ON DELETE CASCADE,
    fecha_inscripcion   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_cancelacion   TIMESTAMP WITH TIME ZONE,
    recordatorio_activo BOOLEAN NOT NULL DEFAULT FALSE,
    asistio             BOOLEAN,
    estado              VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',  -- ACTIVA, CANCELADA, ASISTIDA
    CONSTRAINT uk_inscripcion UNIQUE (estudiante_id, actividad_id)
);

-- MENTORES
CREATE TABLE mentores (
    id                      BIGSERIAL PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    calificacion_promedio   DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    sesiones_completadas    INTEGER NOT NULL DEFAULT 0,
    sesiones_activas        INTEGER NOT NULL DEFAULT 0,         -- máx 5
    disponibilidad          VARCHAR(500),                       -- JSON string: [{"dia":"LUNES","inicio":"08:00","fin":"10:00"}]
    bio                     TEXT,
    activo                  BOOLEAN NOT NULL DEFAULT FALSE,      -- inicia inactivo hasta aprobación
    fecha_solicitud         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- MATERIAS DE MENTOR (fortalezas declaradas)
CREATE TABLE mentor_materias (
    mentor_id   BIGINT NOT NULL REFERENCES mentores(id) ON DELETE CASCADE,
    materia     VARCHAR(120) NOT NULL,
    PRIMARY KEY (mentor_id, materia)
);

-- SOLICITUDES DE MENTORÍA
CREATE TABLE solicitudes_mentoria (
    id                      BIGSERIAL PRIMARY KEY,
    estudiante_id           BIGINT NOT NULL REFERENCES usuarios(id),
    mentor_id               BIGINT NOT NULL REFERENCES mentores(id),
    porcentaje_compatibilidad DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    estado                  VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADA, RECHAZADA, COMPLETADA
    motivacion              TEXT,
    numero_whatsapp         VARCHAR(20),
    fecha_solicitud         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    fecha_respuesta         TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_solicitudes_estudiante ON solicitudes_mentoria(estudiante_id);
CREATE INDEX idx_solicitudes_mentor     ON solicitudes_mentoria(mentor_id);
CREATE INDEX idx_solicitudes_estado     ON solicitudes_mentoria(estado);

-- RECORDATORIOS (Dashboard del estudiante)
CREATE TABLE recordatorios (
    id                  BIGSERIAL PRIMARY KEY,
    estudiante_id       BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    descripcion         TEXT NOT NULL,
    fecha_vencimiento   TIMESTAMP WITH TIME ZONE NOT NULL,
    prioridad           VARCHAR(20) NOT NULL DEFAULT 'PROXIMO',  -- URGENTE, HOY, PROXIMO
    recibir_ventanas    BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_actividades BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_mentoria    BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_rrss        BOOLEAN NOT NULL DEFAULT TRUE,
    numero_whatsapp     VARCHAR(20)
);

-- CONFIGURACION DE NOTIFICACIONES (1:1 con usuario)
CREATE TABLE configuracion_notificaciones (
    id                      BIGSERIAL PRIMARY KEY,
    usuario_id              BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    recibir_tramites        BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_actividades     BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_mentoria        BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_rrss            BOOLEAN NOT NULL DEFAULT TRUE,
    recibir_bot             BOOLEAN NOT NULL DEFAULT TRUE,
    modificado_mentoria     BOOLEAN NOT NULL DEFAULT FALSE,
    numero_whatsapp         VARCHAR(20),
    fcm_token               VARCHAR(500)
);

-- NOTIFICACIONES
CREATE TABLE notificaciones (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    publicacion_id  BIGINT,
    tipo            VARCHAR(50) NOT NULL,  -- TRAMITE, MENTORIA, ACTIVIDAD, RRSS, BOT
    titulo          VARCHAR(200) NOT NULL,
    mensaje         TEXT NOT NULL,
    leida           BOOLEAN NOT NULL DEFAULT FALSE,
    prioridad       VARCHAR(20) NOT NULL DEFAULT 'MEDIA', -- ALTA, MEDIA, BAJA
    fecha           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    url_accion      VARCHAR(500),
    es_activa       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notificaciones_usuario ON notificaciones(usuario_id);
CREATE INDEX idx_notificaciones_leida   ON notificaciones(leida);

-- PUBLICACIONES RRSS (caché local en servidor)
CREATE TABLE publicaciones_rrss (
    id                  BIGSERIAL PRIMARY KEY,
    red_social          VARCHAR(30) NOT NULL,   -- INSTAGRAM, FACEBOOK, TWITTER
    perfil_nombre       VARCHAR(120),
    contenido_texto     TEXT,
    imagen_url          VARCHAR(500),
    url_publicacion     VARCHAR(500),
    hashtags            VARCHAR(500),           -- separados por coma
    likes               INTEGER NOT NULL DEFAULT 0,
    comentarios         INTEGER NOT NULL DEFAULT 0,
    fecha_publicacion   TIMESTAMP WITH TIME ZONE,
    fecha_cache         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    sincronizada        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_rrss_red_social ON publicaciones_rrss(red_social);
CREATE INDEX idx_rrss_fecha      ON publicaciones_rrss(fecha_publicacion DESC);
```

### V2__add_login_attempt_control.sql

```sql
-- Control de intentos fallidos de login (bloqueo 15 min tras 5 intentos)
ALTER TABLE usuarios
    ADD COLUMN intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_hasta   TIMESTAMP WITH TIME ZONE;
```

---

## 6. Entidades JPA del dominio (`domain/model/`)

### Instrucción al agente

Implementa **todas** las entidades siguiendo este contrato exacto. Usa herencia `SINGLE_TABLE` para `Usuario`. Usa Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Aplica `@PreUpdate` para `fechaActualizacion`.

### 6.1 `Usuario.java` (clase base abstracta)

```java
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public abstract class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(name = "primer_nombre", nullable = false, length = 80)
    private String primerNombre;

    @Column(name = "primer_apellido", nullable = false, length = 80)
    private String primerApellido;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Método de negocio: validar contraseña (delegado a service)
    // Método de negocio: actualizar perfil (solo teléfono, foto, correo)
    // Método de negocio: cambiar rol/estado (solo Admin)
}
```

### 6.2 `Estudiante.java`

```java
@Entity
@DiscriminatorValue("Estudiante")
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Estudiante extends Usuario {
    @Column(name = "correo_institucional", length = 120)
    private String correoInstitucional;  // debe terminar en @uniminuto.edu.co

    @Column(name = "programa_academico", length = 120)
    private String programaAcademico;

    @Column
    private Integer semestre;

    @Column
    private String sede;

    @Column(name = "promedio_academico")
    private Double promedioAcademico;

    @Column(name = "estado_cuenta", length = 30)
    private String estadoCuenta = "ACTIVA";

    // Relaciones
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToOne(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Mentor perfilMentor;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recordatorio> recordatorios = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConfiguracionNotificaciones configuracionNotificaciones;

    // Métodos de negocio (retornar DTOs vía service):
    // obtenerDashboard(), generarMentorRecomendaciones(), calcularEstadisticas()
}
```

### 6.3 `Administrador.java`

```java
@Entity
@DiscriminatorValue("Administrador")
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Administrador extends Usuario {
    @Column(name = "nivel_acceso", length = 30)
    private String nivelAcceso = "ADMIN";
    // Puede: registrar/consultar estudiantes, crear/editar actividades
}
```

### 6.4 `Sesion.java`

```java
@Entity @Table(name = "sesiones")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Sesion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_jwt", nullable = false, unique = true, columnDefinition = "TEXT")
    private String tokenJwt;

    @Column(name = "refresh_token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "dispositivo_info", length = 255)
    private String dispositivoInfo;

    // Métodos de negocio
    public boolean isValido() { return activo && fechaExpiracion.isAfter(LocalDateTime.now()); }
    public void invalidar() { this.activo = false; }
    public boolean estaExpirado() { return fechaExpiracion.isBefore(LocalDateTime.now()); }
}
```

### 6.5 `Actividad.java`

```java
@Entity @Table(name = "actividades")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Actividad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 200)
    private String lugar;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaActividad categoria;

    @Column(name = "cupo_total", nullable = false)
    private Integer cupoTotal;

    @Column(name = "cupo_disponible", nullable = false)
    private Integer cupoDisponible;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "recordatorio_wa", nullable = false)
    private Boolean recordatorioWa = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Administrador administrador;

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() { fechaCreacion = LocalDateTime.now(); }

    // Métodos de negocio
    public boolean tieneCupo() { return cupoDisponible > 0; }
    public void decrementarCupo() {
        if (!tieneCupo()) throw new IllegalStateException("Sin cupos disponibles");
        cupoDisponible--;
    }
    public void incrementarCupo() { cupoDisponible = Math.min(cupoDisponible + 1, cupoTotal); }
    public boolean esCancelable(LocalDateTime ahora) {
        return fechaHora.minusHours(2).isAfter(ahora);
    }
}
```

### 6.6 `Inscripcion.java`

```java
@Entity @Table(name = "inscripciones",
    uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id","actividad_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Inscripcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "recordatorio_activo", nullable = false)
    private Boolean recordatorioActivo = false;

    @Column
    private Boolean asistio;

    @Column(nullable = false, length = 30)
    private String estado = "ACTIVA";  // ACTIVA, CANCELADA, ASISTIDA

    @PrePersist
    protected void onCreate() { fechaInscripcion = LocalDateTime.now(); }

    public void confirmar() { this.estado = "ACTIVA"; }
    public void cancelar() {
        this.estado = "CANCELADA";
        this.fechaCancelacion = LocalDateTime.now();
    }
    public void marcarAsistencia() { this.asistio = true; this.estado = "ASISTIDA"; }
}
```

### 6.7 `Mentor.java`

```java
@Entity @Table(name = "mentores")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Mentor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    @Column(name = "calificacion_promedio", nullable = false)
    private Double calificacionPromedio = 0.0;

    @Column(name = "sesiones_completadas", nullable = false)
    private Integer sesionesCompletadas = 0;

    @Column(name = "sesiones_activas", nullable = false)
    private Integer sesionesActivas = 0;  // constraint: máx 5

    @Column(name = "disponibilidad", length = 500)
    private String disponibilidad;  // JSON: [{"dia":"LUNES","inicio":"08:00","fin":"10:00"}]

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private Boolean activo = false;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @ElementCollection
    @CollectionTable(name = "mentor_materias", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "materia", length = 120)
    private List<String> materias = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    private List<SolicitudMentoria> solicitudes = new ArrayList<>();

    @PrePersist
    protected void onCreate() { fechaSolicitud = LocalDateTime.now(); }

    public boolean tieneCapacidad() { return sesionesActivas < 5; }
    public void calcularCompatibilidad(Estudiante e, Long idEstudiante) { /* delegado a service */ }
}
```

### 6.8 `SolicitudMentoria.java`

```java
@Entity @Table(name = "solicitudes_mentoria")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SolicitudMentoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(name = "porcentaje_compatibilidad", nullable = false)
    private Double porcentajeCompatibilidad = 0.0;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";  // PENDIENTE, CONFIRMADA, RECHAZADA, COMPLETADA

    @Column(columnDefinition = "TEXT")
    private String motivacion;

    @Column(name = "numero_whatsapp", length = 20)
    private String numeroWhatsapp;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @PrePersist
    protected void onCreate() { fechaSolicitud = LocalDateTime.now(); }

    public void confirmar() { this.estado = "CONFIRMADA"; this.fechaRespuesta = LocalDateTime.now(); }
    public void rechazar(String motivo) { this.estado = "RECHAZADA"; this.fechaRespuesta = LocalDateTime.now(); }
}
```

### 6.9 `Recordatorio.java`

```java
@Entity @Table(name = "recordatorios")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Recordatorio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(nullable = false, length = 20)
    private String prioridad = "PROXIMO";  // URGENTE, HOY, PROXIMO

    // Preferencias reutilizadas como columnas booleanas
    @Column(name = "recibir_ventanas", nullable = false) private Boolean recibirVentanas = true;
    @Column(name = "recibir_actividades", nullable = false) private Boolean recibirActividades = true;
    @Column(name = "recibir_mentoria", nullable = false) private Boolean recibirMentoria = true;
    @Column(name = "recibir_rrss", nullable = false) private Boolean recibirRrss = true;
    @Column(name = "numero_whatsapp", length = 20) private String numeroWhatsapp;

    public void programar() { /* lógica de scheduling */ }
    public void cancelar() { /* desactivar recordatorio */ }
    public boolean disparar() { return LocalDateTime.now().isAfter(fechaVencimiento.minusHours(1)); }
}
```

### 6.10 `ConfiguracionNotificaciones.java`

```java
@Entity @Table(name = "configuracion_notificaciones")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfiguracionNotificaciones {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "recibir_tramites", nullable = false) private Boolean recibirTramites = true;
    @Column(name = "recibir_actividades", nullable = false) private Boolean recibirActividades = true;
    @Column(name = "recibir_mentoria", nullable = false) private Boolean recibirMentoria = true;
    @Column(name = "recibir_rrss", nullable = false) private Boolean recibirRrss = true;
    @Column(name = "recibir_bot", nullable = false) private Boolean recibirBot = true;
    @Column(name = "modificado_mentoria", nullable = false) private Boolean modificadoMentoria = false;
    @Column(name = "numero_whatsapp", length = 20) private String numeroWhatsapp;
    @Column(name = "fcm_token", length = 500) private String fcmToken;

    public void actualizarPreferencias(Map<String, Boolean> prefs) { /* setter dinámico */ }
    public void establecerTipoNotificacion(String tipo, Boolean activo) { /* … */ }
    public boolean esEficiente(String tipo) { /* verifica si el tipo está activo */ return true; }
}
```

### 6.11 `Notificacion.java`

```java
@Entity @Table(name = "notificaciones")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "publicacion_id")
    private Long publicacionId;

    @Column(nullable = false, length = 50)
    private String tipo;  // TRAMITE, MENTORIA, ACTIVIDAD, RRSS, BOT

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(nullable = false, length = 20)
    private String prioridad = "MEDIA";  // ALTA, MEDIA, BAJA

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "url_accion", length = 500)
    private String urlAccion;

    @Column(name = "es_activa", nullable = false)
    private Boolean esActiva = true;

    @PrePersist
    protected void onCreate() { fecha = LocalDateTime.now(); }

    public void marcarLeida() { this.leida = true; }
    public void marcarActiva(Boolean estado) { this.esActiva = estado; }
}
```

### 6.12 `PublicacionRRSS.java`

```java
@Entity @Table(name = "publicaciones_rrss")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicacionRRSS {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "red_social", nullable = false, length = 30)
    private String redSocial;  // INSTAGRAM, FACEBOOK, TWITTER

    @Column(name = "perfil_nombre", length = 120)
    private String perfilNombre;

    @Column(name = "contenido_texto", columnDefinition = "TEXT")
    private String contenidoTexto;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "url_publicacion", length = 500)
    private String urlPublicacion;

    @Column(length = 500)
    private String hashtags;  // separados por coma

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Integer comentarios = 0;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cache", nullable = false)
    private LocalDateTime fechaCache;

    @Column(nullable = false)
    private Boolean sincronizada = true;

    @PrePersist
    protected void onCreate() { fechaCache = LocalDateTime.now(); }

    public void obtenerNuevasPublicaciones() { /* llamado por scheduler */ }
    public void filtrarPorHashtag(String tag) { /* filtro en service */ }
    public List<PublicacionRRSS> obtenerPorHashtag(String tag) { return new ArrayList<>(); }
}
```

### 6.13 Enum `CategoriaActividad.java`

```java
public enum CategoriaActividad {
    CULTURAL, DEPORTES, APOYO, SALUD
}
```

---

## 7. Repositorios JPA (`domain/repository/`)

```java
// ActividadRepository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findAll();
    List<Actividad> findByFechaData(LocalDateTime date, Long actividadId); // filtro por fecha/categoría
    Optional<Actividad> findByIdAndActivaTrue(Long id);
    void actualizarCupos(Long id, Integer delta); // via @Modifying @Query
}

// UsuarioRepository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findById(Long id);
    boolean existsByCorreo(String correo);
    boolean existsByNumeroDocumento(String doc);
}

// MentorRepository
public interface MentorRepository extends JpaRepository<Mentor, Long> {
    List<Mentor> findByActivoTrue();
    Optional<Mentor> findByEstudianteId(Long id);
    Optional<Mentor> findById(Long id);
    void updateSesionesActivasWhenId(Long id, Delta delta);
}

// PublicacionRRSSRepository
public interface PublicacionRRSSRepository extends JpaRepository<PublicacionRRSS, Long> {
    List<PublicacionRRSS> findByRedSocialOrderByFechaPublicacionDesc(String redSocial);
    List<PublicacionRRSS> findByHashtagsContaining(String hashtag);
    List<PublicacionRRSS> findByRedSocial(String redSocial);
    void deleteOlderThan(LocalDateTime fecha);
}

// NotificacionRepository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<Notificacion> findByReceivedUserId(Long userId, Long keep); // solo últimas N
    List<Notificacion> findReceivedNotificaciones(Long userId);
    void deleteLeidasByUsuarioId(Long usuarioId, Long keep);
}
```

> **Nota al agente:** implementa las firmas anteriores usando anotaciones `@Query(JPQL)` o Spring Data naming conventions donde aplique. Todos los repositorios deben extender `JpaRepository<T, Long>`.

---

## 8. Capa de servicios (`application/service/`)

### 8.1 `AuthService.java`

**Responsabilidades:**
- `login(String correo, String password)` → valida dominio `@uniminuto.edu.co`, verifica bcrypt, aplica bloqueo tras 5 intentos (15 min), genera JWT (30 min) + refresh token (7 días), persiste `Sesion`.
- `refreshToken(String refreshToken)` → valida refresh, emite nuevo JWT.
- `logout(String token)` → invalida la `Sesion` activa.
- `validarToken(String token)` → retorna `Boolean`.
- `actualizarFcmToken(String token, String fcmToken)` → actualiza el token de FCM en `ConfiguracionNotificaciones`.
- `makeFakeToken(String correo)` → **NUNCA** implementar; rechazar siempre.

**Reglas de negocio críticas:**
1. El mensaje de error ante credenciales incorrectas es **siempre** `"Credenciales inválidas"` (no indicar qué campo falla).
2. Sesión expira a los 30 min de inactividad. El campo `fecha_expiracion` en `sesiones` se renueva con cada petición autenticada.
3. Contraseñas con `BCryptPasswordEncoder` con `strength = 12`.
4. Si `bloqueadoHasta` existe y está en el futuro → lanzar `AccountLockedException` con tiempo restante.
5. Dominio de correo validado con regex: `^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu\\.co$`.

### 8.2 `ActividadService.java`

**Responsabilidades:**
- `listarActividades(FiltroActividadDTO filtro)` → filtros por categoría, fecha, "con recordatorio WA".
- `inscribirse(Long estudianteId, Long actividadId)` → verificar cupo > 0 (optimistic lock), decrementar, persistir `Inscripcion`, enviar notificación push si FCM token activo.
- `cancelarInscripcion(Long inscripcionId, Long estudianteId)` → verificar que `fechaHora - 2h > now()`, incrementar cupo, actualizar estado.
- `registrarAsistencia(Long actividadId, Long estudianteId)` → solo `Administrador` puede llamar.
- `actualizarCupos(Long id, Integer delta)` → transaccional, usado internamente.
- `obtenerActividadesPorEstudiante(Long estudianteId)` → historial de inscripciones.

**Reglas de negocio críticas:**
1. El decremento de cupo debe hacerse dentro de una transacción `@Transactional` con `@Lock(LockModeType.PESSIMISTIC_WRITE)` sobre `Actividad` para evitar race condition.
2. Si `cupoDisponible == 0` → lanzar `SinCuposException` (HTTP 409).
3. Si se cancela después del límite de 2 horas → lanzar `CancelacionFueraDeplazoException` (HTTP 422).

### 8.3 `MentoriaService.java`

**Responsabilidades:**
- `recomendarMentores(Long estudianteId)` → ejecutar algoritmo de compatibilidad (ver §8.3.1).
- `findMentorById(Long id)` → retorna `Mentor`.
- `calcularCompatibilidad(Long estudianteId, Long mentorId)` → retorna `Double` (0–100).
- `postularComoMentor(Long estudianteId, PostulacionMentorDTO dto)` → crear registro `Mentor` con `activo = false` (requiere aprobación de Admin).
- `aprobarMentor(Long mentorId)` → solo Admin, activa el mentor.
- `rechazarMentor(Long mentorId)` → solo Admin.
- `solicitarMentoria(Long estudianteId, SolicitudMentoriaDTO dto)` → crea `SolicitudMentoria`, verifica que el mentor tenga < 5 sesiones activas.
- `confirmarMentoria(String mentorId)` → mentor confirma solicitud, incrementa `sesionesActivas`.
- `rechazarMentoria(String id)` → mentor rechaza solicitud.
- `actualizarSesionesActivas(Long id, Integer delta)` → actualiza contador, valida máx 5.

#### 8.3.1 Algoritmo de compatibilidad (ejecutar en servidor)

```
compatibilidad = 0.0

PESO_MATERIA      = 40   // coincidencia entre materias del mentor y materias con dificultad del estudiante
PESO_CALIFICACION = 30   // calificación promedio del mentor (normalizada a 0–100)
PESO_SESIONES     = 20   // sesiones completadas (normalizado: min=0, max=50)
PESO_DISPONIB     = 10   // solapamiento horario entre estudiante y mentor

Para cada mentor activo:
  1. materias_coincidentes = intersección(mentor.materias, estudiante.materiasConDificultad)
  2. score_materia = (|materias_coincidentes| / max(|estudiante.materiasConDificultad|, 1)) * PESO_MATERIA
  3. score_calificacion = (mentor.calificacionPromedio / 5.0) * PESO_CALIFICACION
  4. score_sesiones = min(mentor.sesionesCompletadas / 50.0, 1.0) * PESO_SESIONES
  5. score_disponib = calcularSolapamientoHorario(estudiante, mentor) * PESO_DISPONIB
  6. compatibilidad = score_materia + score_calificacion + score_sesiones + score_disponib

Ordenar mentores por compatibilidad DESC.
```

**Regla crítica:** Los datos académicos del estudiante (promedio, historial) **NO** se exponen al mentor en ningún endpoint.

### 8.4 `NotificacionService.java`

**Responsabilidades:**
- `enviarPushNotificacion(String fcmToken, String titulo, String cuerpo, Map<String, String> data)` → llama a Firebase Admin SDK.
- `obtenerNotificacionesPorUsuario(Long id)` → retorna máx 20 ordenadas por fecha DESC; elimina las más antiguas automáticamente si supera 20.
- `marcarLeida(Long notifId, Long usuarioId)` → actualiza `leida = true`.
- `marcarActiva(Long usuarioId, Boolean activo)` → actualiza estado.
- `eliminarAntiguas(Long usuarioId, Long keep)` → housekeeping.
- `nuevaPublicacionInstitucional(Long publicacionId, List<String> fcmTokens)` → envío masivo (usa FCM batch API).

**Tipos de notificación y restricciones:**
- `ALTA` prioridad: el estudiante **NO puede** desactivarlas.
- Máx 20 notificaciones visibles; las más antiguas se purgan automáticamente.
- Al tocar una notificación el cliente navega via `urlAccion`.

### 8.5 `RRSSService.java`

**Responsabilidades:**
- `obtenerFeed(String redSocial, String hashtag)` → retorna publicaciones cacheadas ordenadas por `fechaPublicacion DESC`.
- `sincronizarFeed()` → `@Scheduled(fixedRate = 900000)` — cada 15 min llama adaptadores externos y actualiza caché.
- `filtrarPorHashtag(String hashtag)` → búsqueda en campo `hashtags`.
- `filtrarPorRedSocial(String redSocial)` → filtro simple.
- `obtenerTodasPublicaciones(String redSocial)` → lista paginada.
- `obtenerProximaMentoria(String tag)` → helpers de búsqueda.

**Patrón Adapter para redes sociales:**
```
interface RedSocialAdapter {
    List<PublicacionRRSSDTO> obtenerPublicaciones();
    String getNombre();
}

class InstagramAdapter implements RedSocialAdapter { ... }
class FacebookAdapter  implements RedSocialAdapter { ... }
class TwitterAdapter   implements RedSocialAdapter { ... }
```
El `RRSSService` itera sobre todos los adaptadores registrados como beans de Spring para facilitar agregar nuevas redes sin modificar el core.

**Restricciones:**
- La app NO puede publicar en redes sociales, solo leer.
- En caso de error de API externa → retornar últimas publicaciones cacheadas (modo offline).
- Credenciales de APIs externas en variables de entorno: `INSTAGRAM_TOKEN`, `FACEBOOK_TOKEN`, `TWITTER_BEARER_TOKEN`.

---

## 9. Capa de seguridad (`infrastructure/security/`)

### 9.1 `JwtUtil.java`

```java
// Métodos obligatorios:
String generateToken(UserDetails userDetails, Long userId, String role);
String generateRefreshToken(UserDetails userDetails);
String extractUsername(String token);
Long extractUserId(String token);
boolean isTokenValid(String token, UserDetails userDetails);
boolean isTokenExpired(String token);
Date extractExpiration(String token);
```

Usa `HS512` como algoritmo. La clave se genera desde `jwt.secret` (Base64 decode). Claims adicionales: `userId`, `role`, `sessionId`.

### 9.2 `JwtAuthFilter.java`

```java
// extends OncePerRequestFilter
// Flujo:
// 1. Extraer "Authorization: Bearer <token>" del header
// 2. Extraer username via JwtUtil
// 3. Cargar UserDetails via UserDetailsServiceImpl
// 4. Validar token
// 5. Verificar que la Sesion en DB esté activa (previene tokens robados)
// 6. Setear SecurityContextHolder
// 7. Renovar fecha_expiracion de la Sesion (sliding expiration 30 min)
```

### 9.3 `SecurityConfig.java`

```java
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    // Rutas públicas: POST /api/v1/auth/login, POST /api/v1/auth/refresh,
    //                 GET  /api/v1/actuator/health, GET /swagger-ui/**, GET /v3/api-docs/**
    // Todo lo demás: autenticado

    // CORS: permitir origen configurado vía env variable CORS_ALLOWED_ORIGINS
    // CSRF: desactivado (API stateless con JWT)
    // Session: STATELESS
    // BCryptPasswordEncoder bean con strength=12
}
```

### 9.4 `UserDetailsServiceImpl.java`

```java
// Carga usuario por correo desde UsuarioRepository
// Convierte rol a GrantedAuthority: ROLE_ESTUDIANTE | ROLE_ADMINISTRADOR
// Valida que activo == true y no esté bloqueado
```

---

## 10. Controladores REST (`api/controller/`)

Todos los controladores deben:
- Usar `@RestController @RequestMapping("/api/v1/{modulo}")`.
- Documentar con anotaciones OpenAPI 3 (`@Operation`, `@ApiResponse`, `@Tag`).
- Retornar `ResponseEntity<ApiResponse<T>>` usando el wrapper definido en §11.
- Validar DTOs de entrada con `@Valid`.

### 10.1 `AuthController.java`

| Método | Ruta | Body | Respuesta | Rol |
|---|---|---|---|---|
| POST | `/auth/login` | `LoginRequest` | `LoginResponse` (token, refreshToken, usuario) | Público |
| POST | `/auth/refresh` | `RefreshTokenRequest` | `LoginResponse` | Público |
| POST | `/auth/logout` | — (header Bearer) | `void` | Autenticado |
| PUT | `/auth/fcm-token` | `FcmTokenRequest` | `void` | Autenticado |

### 10.2 `ActividadController.java`

| Método | Ruta | Body/Params | Respuesta | Rol |
|---|---|---|---|---|
| GET | `/actividades` | `?categoria=&fecha=&recordatorioWa=` | `Page<ActividadResponse>` | Estudiante |
| GET | `/actividades/{id}` | — | `ActividadResponse` | Estudiante |
| POST | `/actividades` | `ActividadRequest` | `ActividadResponse` | Admin |
| PUT | `/actividades/{id}` | `ActividadRequest` | `ActividadResponse` | Admin |
| DELETE | `/actividades/{id}` | — | `void` | Admin |
| POST | `/actividades/{id}/inscribir` | — | `InscripcionResponse` | Estudiante |
| DELETE | `/actividades/{id}/cancelar` | — | `void` | Estudiante |
| POST | `/actividades/{id}/asistencia/{estudianteId}` | — | `void` | Admin |

### 10.3 `MentoriaController.java`

| Método | Ruta | Body | Respuesta | Rol |
|---|---|---|---|---|
| GET | `/mentorias/mentores` | — | `List<MentorResponse>` con % compatibilidad | Estudiante |
| GET | `/mentorias/mentores/{id}` | — | `MentorDetalleResponse` | Estudiante |
| POST | `/mentorias/postular` | `PostulacionMentorDTO` | `MentorResponse` | Estudiante |
| POST | `/mentorias/solicitar` | `SolicitudMentoriaDTO` | `SolicitudResponse` | Estudiante |
| PUT | `/mentorias/solicitudes/{id}/confirmar` | — | `void` | Estudiante (el mentor) |
| PUT | `/mentorias/solicitudes/{id}/rechazar` | — | `void` | Estudiante (el mentor) |
| GET | `/mentorias/mis-solicitudes` | — | `List<SolicitudResponse>` | Estudiante |
| PUT | `/mentorias/mentores/{id}/aprobar` | — | `void` | Admin |
| PUT | `/mentorias/mentores/{id}/rechazar` | — | `void` | Admin |

### 10.4 `NotificacionController.java`

| Método | Ruta | Body | Respuesta | Rol |
|---|---|---|---|---|
| GET | `/notificaciones` | — | `List<NotificacionResponse>` | Autenticado |
| PUT | `/notificaciones/{id}/leer` | — | `void` | Autenticado |
| PUT | `/notificaciones/leer-todas` | — | `void` | Autenticado |
| GET | `/notificaciones/config` | — | `ConfiguracionNotificacionesResponse` | Autenticado |
| PUT | `/notificaciones/config` | `ConfiguracionNotificacionesRequest` | `void` | Autenticado |

### 10.5 `RRSSController.java`

| Método | Ruta | Params | Respuesta | Rol |
|---|---|---|---|---|
| GET | `/rrss/feed` | `?red=&hashtag=&page=&size=` | `Page<PublicacionRRSSResponse>` | Autenticado |
| POST | `/rrss/sincronizar` | — | `void` | Admin |

### 10.6 `PerfilController.java`

| Método | Ruta | Body | Respuesta | Rol |
|---|---|---|---|---|
| GET | `/perfil` | — | `PerfilResponse` | Autenticado |
| PUT | `/perfil/telefono` | `TelefonoRequest` | `void` | Autenticado |
| PUT | `/perfil/password` | `ChangePasswordRequest` | `void` | Autenticado |
| PUT | `/perfil/foto` | `FotoRequest` | `String fotoUrl` | Autenticado |
| GET | `/perfil/estadisticas` | — | `EstadisticasResponse` | Autenticado |
| GET | `/perfil/historial-asistencias` | — | `List<InscripcionResponse>` | Autenticado |

### 10.7 `DashboardController.java`

| Método | Ruta | Respuesta | Rol |
|---|---|---|---|
| GET | `/dashboard` | `DashboardResponse` | Estudiante |

`DashboardResponse` incluye: promedio, eventos asistidos, mentorías activas, notificaciones no leídas, recordatorios urgentes, próximos eventos.

### 10.8 `AdminController.java`

| Método | Ruta | Body | Respuesta | Rol |
|---|---|---|---|---|
| POST | `/admin/usuarios` | `CrearEstudianteRequest` | `EstudianteResponse` | Admin |
| GET | `/admin/usuarios` | — | `Page<EstudianteResponse>` | Admin |
| PUT | `/admin/usuarios/{id}` | `ActualizarEstudianteRequest` | `EstudianteResponse` | Admin |
| GET | `/admin/actividades/{id}/asistentes` | — | `List<EstudianteResponse>` | Admin |
| POST | `/admin/notificaciones/enviar` | `EnviarNotificacionRequest` | `void` | Admin |

---

## 11. DTOs y estructura de respuesta

### 11.1 Wrapper de respuesta

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String correlationId;  // UUID generado por JwtAuthFilter
}
```

### 11.2 DTOs de entrada (request)

```java
// LoginRequest
@Data public class LoginRequest {
    @NotBlank @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu\\.co$",
             message = "Solo se permiten correos @uniminuto.edu.co")
    private String correo;

    @NotBlank @Size(min = 8, max = 100)
    private String password;
}

// ActividadRequest
@Data public class ActividadRequest {
    @NotBlank @Size(max = 200) private String nombre;
    @Size(max = 200) private String lugar;
    private String descripcion;
    @NotNull @Future private LocalDateTime fechaHora;
    @Min(15) @Max(480) private Integer duracionMinutos;
    @NotNull private CategoriaActividad categoria;
    @Min(1) @Max(500) private Integer cupoTotal;
}

// PostulacionMentorDTO
@Data public class PostulacionMentorDTO {
    @NotEmpty private List<@NotBlank String> materias;
    @NotBlank private String disponibilidad;  // JSON
    @Size(max = 1000) private String bio;
}

// SolicitudMentoriaDTO
@Data public class SolicitudMentoriaDTO {
    @NotNull private Long mentorId;
    @Size(max = 1000) private String motivacion;
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$") private String numeroWhatsapp;
}

// ChangePasswordRequest
@Data public class ChangePasswordRequest {
    @NotBlank private String passwordActual;
    @NotBlank @Size(min = 8, max = 100) private String passwordNueva;
    @NotBlank private String confirmacionPassword;
}
```

### 11.3 DTOs de salida (response)

```java
// LoginResponse
@Data @Builder public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;  // segundos
    private UsuarioBasicoResponse usuario;
}

// UsuarioBasicoResponse
@Data @Builder public class UsuarioBasicoResponse {
    private Long id;
    private String primerNombre;
    private String primerApellido;
    private String correo;
    private String rol;
    private String programaAcademico;
    private Integer semestre;
}

// ActividadResponse
@Data @Builder public class ActividadResponse {
    private Long id;
    private String nombre;
    private String lugar;
    private String descripcion;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private String categoria;
    private Integer cupoTotal;
    private Integer cupoDisponible;
    private Boolean activa;
    private Boolean inscrito;       // true si el estudiante autenticado está inscrito
    private Boolean recordatorioWa;
}

// MentorResponse
@Data @Builder public class MentorResponse {
    private Long id;
    private String nombre;
    private List<String> materias;
    private Double calificacionPromedio;
    private Integer sesionesCompletadas;
    private String disponibilidad;
    private Double porcentajeCompatibilidad;  // calculado en tiempo real
    private String numeroWhatsapp;
    // NUNCA incluir datos académicos del estudiante-mentor
}

// DashboardResponse
@Data @Builder public class DashboardResponse {
    private String nombreEstudiante;
    private String programaAcademico;
    private Double promedioAcademico;
    private Integer eventosAsistidos;
    private Integer mentoriasActivas;
    private Integer notificacionesSinLeer;
    private List<RecordatorioResponse> recordatoriosUrgentes;
    private List<ActividadResponse> proximosEventos;
}
```

---

## 12. Manejo de excepciones

### `GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 401 Unauthorized
    @ExceptionHandler(BadCredentialsException.class)
    // 403 Forbidden  
    @ExceptionHandler(AccessDeniedException.class)
    // 404 Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    // 409 Conflict (sin cupos, usuario duplicado)
    @ExceptionHandler(SinCuposException.class)
    // 422 Unprocessable Entity (cancelación fuera de plazo)
    @ExceptionHandler(CancelacionFueraDeplazoException.class)
    // 423 Locked (cuenta bloqueada)
    @ExceptionHandler(AccountLockedException.class)
    // 500 Internal
    @ExceptionHandler(Exception.class)

    // Todas retornan ApiResponse<ErrorDetail> con:
    // - success: false
    // - message: descripción amigable
    // - timestamp
    // - correlationId (extraído del request context)
    // NUNCA incluir stack traces ni información PII en producción
}
```

### Excepciones personalizadas

```
exception/custom/
├── SinCuposException.java           (RuntimeException)
├── CancelacionFueraDeplazoException.java
├── AccountLockedException.java
├── MentorSinCapacidadException.java
├── DominioNoPermitidoException.java
├── TokenInvalidoException.java
└── RecursoNoEncontradoException.java
```

---

## 13. Configuración Firebase FCM

### `FirebaseConfig.java`

```java
@Configuration
public class FirebaseConfig {
    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(new ClassPathResource(credentialsPath).getInputStream())
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}
```

### `FcmService.java`

```java
@Service
public class FcmService {
    // enviarNotificacion(String fcmToken, String title, String body, Map<String,String> data)
    //   → Message.builder()
    //        .setToken(fcmToken)
    //        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
    //        .putAllData(data)
    //        .build()
    //   → FirebaseMessaging.getInstance().send(message)
    //   → loggear correlationId pero NO datos personales

    // enviarMasivo(List<String> tokens, String title, String body)
    //   → MulticastMessage con hasta 500 tokens por batch
    //   → usar FirebaseMessaging.getInstance().sendMulticast()
}
```

---

## 14. Scheduler de sincronización RRSS

```java
@Component
public class RRSSScheduler {
    @Scheduled(fixedRate = 900_000)  // cada 15 min
    public void sincronizarRedes() {
        // Llama a RRSSService.sincronizarFeed()
        // Loggear inicio y fin con timestamp y resultado
        // En caso de error: loggear excepción (sin PII), no relanzar
    }
}
```

---

## 15. Requerimientos no funcionales que el código debe garantizar

| RNF | Implementación requerida |
|---|---|
| RNF-001 Rendimiento | Paginación obligatoria en listas > 20 items (`Pageable`). Todos los queries críticos deben tener índice en DB. Pool HikariCP configurado (§4). |
| RNF-002 Seguridad | BCrypt strength 12, JWT 30 min + refresh 7 días, HTTPS (configurado en proxy externo), logs sin PII, bloqueo 5 intentos. |
| RNF-002 Ley 1581 | Los datos académicos del estudiante son confidenciales. Nunca exponerlos en endpoints públicos o del mentor. |
| RNF-005 Escalabilidad | Patrón Adapter para RRSS (§8.5). Microservicio-ready: sin estado en memoria de aplicación (tokens solo en DB). API Gateway friendly: todos los endpoints bajo `/api/v1/`. |

---

## 16. Health Check y actuator

```yaml
# En application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
      base-path: /api/v1/actuator
  endpoint:
    health:
      show-details: when-authorized
```

Implementar `HealthIndicator` personalizado para verificar:
1. Conectividad a PostgreSQL.
2. Conectividad a Firebase (ping).
3. Disponibilidad de la tabla `sesiones`.

---

## 17. Convenciones de código

1. **Idioma:** nombres de clases, métodos y variables en **inglés** (excepción: nombres de dominio específicos del negocio ya definidos en los requerimientos, ej. `Inscripcion`, `SolicitudMentoria`).
2. **Lombok obligatorio** en todas las entidades y DTOs. Nunca escribir getters/setters manualmente.
3. **`@Transactional`** en todos los métodos de servicio que modifiquen datos.
4. **`@Transactional(readOnly = true)`** en todos los métodos de consulta.
5. Separación estricta de capas: los **controladores** nunca acceden a repositorios directamente.
6. Los **servicios** nunca retornan entidades JPA; siempre mapean a DTOs via MapStruct.
7. Los **repositorios** nunca contienen lógica de negocio.
8. Toda validación de negocio va en el **servicio**, no en el controlador.
9. **Correlation ID:** generar `UUID` en el filtro de autenticación, propagar en `MDC` de SLF4J, incluir en todos los logs y en las respuestas de error.
10. **Nunca** loggear contraseñas, tokens JWT, ni datos personales (PII).

---

## 18. Orden de implementación recomendado al agente

1. **Configuración del proyecto** — `pom.xml`, `application.yml`, estructura de paquetes.
2. **Migraciones Flyway** — `V1__create_core_tables.sql`, `V2__add_login_attempt_control.sql`.
3. **Entidades JPA** — en este orden: `Usuario` → `Estudiante` → `Administrador` → `Sesion` → `Actividad` → `Inscripcion` → `Mentor` → `SolicitudMentoria` → `Recordatorio` → `ConfiguracionNotificaciones` → `Notificacion` → `PublicacionRRSS`.
4. **Repositorios** — uno por entidad.
5. **Seguridad** — `JwtUtil` → `UserDetailsServiceImpl` → `JwtAuthFilter` → `SecurityConfig`.
6. **Firebase** — `FirebaseConfig` → `FcmService`.
7. **Servicios** — en este orden: `AuthService` → `ActividadService` → `MentoriaService` → `NotificacionService` → `RRSSService`.
8. **Excepciones** — `GlobalExceptionHandler` + excepciones custom.
9. **Mappers** — MapStruct por módulo.
10. **Controladores** — uno por módulo.
11. **Scheduler** — `RRSSScheduler`.
12. **Health checks** — `HealthIndicator` personalizado.
13. **OpenAPI** — `OpenApiConfig` con info del proyecto.

---

## 19. Checklist final de verificación

Antes de entregar el código como completo, verificar:

- [ ] Flyway ejecuta sin errores en BD limpia.
- [ ] `POST /api/v1/auth/login` con correo no `@uniminuto.edu.co` retorna 400.
- [ ] `POST /api/v1/auth/login` con credenciales incorrectas retorna 401 con mensaje genérico.
- [ ] Tras 5 intentos fallidos, el usuario queda bloqueado 15 minutos.
- [ ] `POST /api/v1/actividades/{id}/inscribir` con cupo=0 retorna 409.
- [ ] `DELETE /api/v1/actividades/{id}/cancelar` menos de 2 horas antes del evento retorna 422.
- [ ] Endpoint de mentor **no** expone datos académicos del estudiante.
- [ ] Notificaciones: máx 20 visibles por usuario; las más antiguas se eliminan.
- [ ] El algoritmo de compatibilidad se ejecuta en el servidor (`MentoriaService`), no en el cliente.
- [ ] `@Transactional` presente en todos los métodos que escriben en BD.
- [ ] Logs no contienen contraseñas ni tokens.
- [ ] Correlation ID presente en todas las respuestas de error.
- [ ] `application.yml` sin secretos hardcodeados (todo en variables de entorno).
- [ ] Swagger UI disponible en `/api/v1/swagger-ui/index.html`.
- [ ] Health check responde en `/api/v1/actuator/health`.
