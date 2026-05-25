# UniRed Backend - Arquitectura, Seguridad, Deployment y API

## Tabla de contenidos

1. [Arquitectura del backend](#1-arquitectura-del-backend)
2. [Seguridad implementada y cumplimiento normativo colombiano](#2-seguridad-implementada-y-cumplimiento-normativo-colombiano)
3. [Deployment](#3-deployment)
4. [Instrucción de uso de APIs](#4-instrucción-de-uso-de-apis)
5. [Endpoints públicos y reservados](#5-endpoints-públicos-y-reservados)
6. [Estado actual de registro de usuarios](#6-estado-actual-de-registro-de-usuarios)
7. [Fuentes técnicas revisadas](#7-fuentes-técnicas-revisadas)

---

## 1) Arquitectura del backend

### 1.1 Estilo arquitectónico

El backend está implementado con una **arquitectura por capas (layered architecture)** y separación por responsabilidades:

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| **API / Presentation** | `api/controller` | Endpoints REST, validación de DTOs, delegación a servicios |
| **Application** | `application/service`, `application/dto`, `application/mapper` | Casos de uso, lógica de negocio, contratos request/response, mapeo DTO <-> entidad (MapStruct) |
| **Domain** | `domain/model`, `domain/repository`, `domain/enums` | Entidades JPA, repositorios, enumeraciones del dominio |
| **Infrastructure** | `infrastructure/security`, `infrastructure/firebase`, `infrastructure/external`, `infrastructure/scheduler`, `infrastructure/health` | Seguridad JWT, integración Firebase FCM, adaptadores de redes sociales, tareas programadas, health checks |
| **Cross-cutting** | `config`, `exception`, `util` | Configuración de seguridad, DB, CORS, Firebase, OpenAPI, manejo global de errores, constantes |

### 1.2 Estructura principal de paquetes

```
src/main/java/com/unired/
├── api/
│   └── controller/          # AuthController, ActividadController, MentoriaController, etc.
├── application/
│   ├── dto/
│   │   ├── request/         # LoginRequest, ActividadRequest, etc.
│   │   └── response/        # ApiResponse, LoginResponse, EstudianteResponse, etc.
│   ├── mapper/              # MapStruct mappers (UsuarioMapper, ActividadMapper, etc.)
│   └── service/             # AuthService, ActividadService, MentoriaService, etc.
├── config/                  # SecurityConfig, DatabaseConfig, OpenApiConfig, JwtConfig, WebConfig, FirebaseConfig
├── domain/
│   ├── enums/               # CategoriaActividad, etc.
│   ├── model/               # Usuario, Estudiante, Administrador, Sesion, Actividad, etc.
│   └── repository/          # JPA repositories
├── exception/
│   ├── custom/              # Excepciones de negocio (SinCuposException, RecursoNoEncontradoException, etc.)
│   └── GlobalExceptionHandler.java
├── infrastructure/
│   ├── external/            # RedSocialAdapter, InstagramAdapter, FacebookAdapter, TwitterAdapter
│   ├── firebase/            # FcmService
│   ├── health/              # UniRedHealthIndicator
│   ├── scheduler/           # RRSSScheduler
│   └── security/            # JwtAuthFilter, JwtUtil, AppUserDetails, UserDetailsServiceImpl
├── util/
│   └── constants/           # SecurityConstants
└── UniRedApplication.java
```

### 1.3 Flujo típico de una petición

1. **Cliente** consume endpoint REST (Controller)
2. **Controller** valida DTO (`@Valid`) y delega en **Service**
3. **Service** aplica reglas de negocio, usa **Repository** para persistencia
4. Se responde siempre con `ApiResponse<T>`
5. Cualquier excepción es capturada por `GlobalExceptionHandler` y retornada con formato `ErrorDetail`

### 1.4 Modelo de datos de usuarios

Se usa **herencia en tabla única** (`usuarios`) con columna discriminadora `dtype`:

- `dtype = 'Estudiante'` -> campos adicionales: `correo_institucional`, `programa_academico`, `semestre`, `sede`, `promedio_academico`, `estado_cuenta`
- `dtype = 'Administrador'` -> campo adicional: `nivel_acceso`

Tabla `sesiones` para control de JWT y refresh tokens activos con expiración deslizante.

### 1.5 Tecnologías principales

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Spring Boot | 3.5.13 | Framework principal |
| Java | 21 | Lenguaje |
| Spring Security | integrado | Autenticación y autorización |
| Spring Data JPA | integrado | Persistencia |
| Hibernate | integrado | ORM |
| PostgreSQL (Supabase) | remoto | Base de datos |
| Flyway | integrado | Migraciones de esquema |
| JWT (JJWT) | 0.12.3 | Tokens de acceso |
| BCrypt | cost 12 | Hash de contraseñas |
| Lombok | integrado | Reducción de boilerplate |
| MapStruct | 1.5.5.Final | Mapeo DTO <-> entidad |
| SpringDoc OpenAPI | 2.8.9 | Documentación Swagger |
| Firebase Admin SDK | 9.2.0 | Push notifications (FCM) |
| HikariCP | integrado | Connection pool |
| Spring Actuator | integrado | Health y métricas |
| Spring DevTools | integrado | Hot reload en desarrollo |

### 1.6 Configuración de la aplicación

```yaml
server:
  port: 3009
  servlet.context-path: /api/v1

spring:
  datasource: PostgreSQL en Supabase (TLS requerido)
  jpa.hibernate.ddl-auto: validate (esquema gestionado por Flyway)
  flyway: habilitado con baseline-on-migrate

jwt:
  expiration-ms: 1800000        # 30 minutos
  refresh-expiration-ms: 604800000  # 7 días
```

---

## 2) Seguridad implementada y cumplimiento normativo colombiano

### 2.1 Controles técnicos de seguridad

#### 2.1.1 Autenticación
| Control | Detalle |
|---------|---------|
| **JWT Access Token** | Expiración configurable (default: 30 min), firmado con HS512 |
| **JWT Refresh Token** | Expiración configurable (default: 7 días), almacenado en DB |
| **Sesiones en DB** | Tabla `sesiones` con expiración deslizante; cada request válido renueva 30 min |
| **Logout** | Invalidación explícita de sesión en DB |
| **Validación de dominio** | Solo correos `@uniminuto.edu.co` pueden autenticarse |

#### 2.1.2 Protección de credenciales
| Control | Detalle |
|---------|---------|
| **BCrypt** | Cost factor 12, no se almacena password en texto plano |
| **Bloqueo por intentos** | 5 intentos fallidos -> bloqueo temporal de 15 minutos |
| **Reset de contador** | Intentos se reinician tras login exitoso o tras desbloqueo |

#### 2.1.3 Autorización
| Control | Detalle |
|---------|---------|
| **Roles** | `ROLE_ESTUDIANTE` y `ROLE_ADMINISTRADOR` |
| **@PreAuthorize** | Protección a nivel de método en cada endpoint |
| **Default deny** | Todo endpoint no explícitamente permitido requiere autenticación |

#### 2.1.4 Protección de comunicaciones
| Control | Detalle |
|---------|---------|
| **TLS a DB** | `sslmode=require` en conexión PostgreSQL |
| **CORS configurable** | Variable `CORS_ALLOWED_ORIGINS`, permite credenciales |
| **Headers de seguridad** | `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `X-XSS-Protection: 0` |
| **CSRF deshabilitado** | Adecuado para API stateless con JWT |

#### 2.1.5 Trazabilidad y observabilidad
| Control | Detalle |
|---------|---------|
| **Correlation ID** | UUID por request, propagado en logs y respuestas |
| **Health checks** | Verificación de PostgreSQL, Firebase y tabla sesiones |
| **Manejo uniforme de errores** | Sin exposición de stack traces al cliente |

### 2.2 Alineación con normativa colombiana

#### Ley 1581 de 2012 (Protección de Datos Personales)

**Fortalezas actuales:**

- Seguridad de acceso mediante autenticación y autorización basada en roles
- Protección de credenciales con algoritmo criptográfico (BCrypt cost 12)
- Trazabilidad técnica mediante correlationId en cada transacción
- Control de errores y validaciones que previenen exposición indebida de datos
- Conexión cifrada a base de datos (TLS)
- Control de sesiones con expiración y revocación

**Brechas identificadas para cumplimiento formal completo:**

| Brecha | Descripción |
|--------|-------------|
| **Consentimiento informado** | No existe módulo ni evidencia de consentimiento explícito del titular para tratamiento de datos |
| **Derechos ARCO** | No se observan endpoints para Acceso, Rectificación, Cancelación y Oposición |
| **Política de retención** | No hay política configurable de retención, anonimización ni borrado seguro de datos |
| **Auditoría de accesos** | No existe bitácora formal de quién accede a qué datos personales (requerido por SIC) |
| **Documento de políticas** | Falta documentación formal de gobierno de datos (RNBD, políticas internas, oficial de protección) |
| **Datos sensibles** | No se ha realizado clasificación de datos sensibles vs. ordinarios |

#### Decreto 1377 de 2013

| Requisito | Estado |
|-----------|--------|
| Políticas de tratamiento | No documentado en la aplicación |
| Manual de procedimientos | No existe |
| Canal de atención para titulares | No implementado |
| Capacitación del personal | Fuera del alcance técnico |
| Registro Nacional de BD (RNBD) | No aplica al software directamente, pero requiere gestión organizacional |

#### Resolución 0423 de 2024 (Ciberseguridad)

| Control | Estado |
|---------|--------|
| Gestión de accesos | Implementado (JWT + roles) |
| Cifrado en tránsito | Parcial (TLS a DB, no se verifica HTTPS en API) |
| Cifrado en reposo | Depende de Supabase (cifrado nativo) |
| Monitoreo y detección | Parcial (actuator health, sin SIEM) |
| Gestión de vulnerabilidades | No automatizado |
| Plan de respuesta a incidentes | No implementado |

### 2.3 Recomendaciones para cerrar brecha legal y técnica

1. **Implementar flujo de consentimiento** con versionado de política de tratamiento de datos
2. **Crear endpoints para ejercicio de derechos de titular**: consulta de datos, actualización, supresión, revocatoria de consentimiento
3. **Definir política de retención**, anonimización y borrado seguro con cron jobs
4. **Incorporar auditoría de acceso a datos personales** (bitácora legal con quién, cuándo, qué dato)
5. **Forzar HTTPS** en producción con HSTS
6. **Formalizar matriz legal** con área jurídica (SIC/RNBD)
7. **Implementar rate limiting** para prevenir fuerza bruta adicional al bloqueo por intentos
8. **Agregar logging de auditoría** para accesos a datos sensibles
9. **Implementar validación de fortaleza de contraseña** (longitud mínima, complejidad)
10. **Agregar Content-Security-Policy** y headers adicionales de seguridad

---

## 3) Deployment

### 3.1 Estado actual del despliegue

| Aspecto | Detalle |
|---------|---------|
| **Framework** | Spring Boot 3.5.13 |
| **Lenguaje** | Java 21 |
| **Base de datos** | PostgreSQL en Supabase (AWS us-east-1) |
| **Migraciones** | Flyway con baseline-on-migrate |
| **Documentación API** | SpringDoc OpenAPI 2.8.9 |
| **Puerto** | 3009 |
| **Context path** | `/api/v1` |

### 3.2 Artefactos de deployment ausentes

Actualmente el repositorio **NO incluye**:
- `Dockerfile`
- `docker-compose.yml`
- Pipelines CI/CD (`.github/workflows`)
- `Procfile` (Heroku/Railway)
- Configuración Kubernetes

### 3.3 Variables de entorno requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_PASSWORD` | Contraseña de Supabase PostgreSQL | `tu_password_supabase` |
| `JWT_SECRET` | Secreto Base64 para firma JWT (mínimo 512 bits) | `BASE64_SECRET_AQUI` |
| `FIREBASE_ENABLED` | Habilitar Firebase FCM (opcional) | `false` |
| `FIREBASE_CREDENTIALS_PATH` | Ruta al JSON de credenciales de Firebase | `classpath:firebase-service-account.json` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos para CORS | `http://localhost:3000` |

### 3.4 Ejecución local

```powershell
# Crear archivo .env en la raíz del proyecto
DB_PASSWORD=tu_password_supabase
JWT_SECRET=tu_secret_base64
FIREBASE_ENABLED=false

# Ejecutar con Maven
.\mvnw spring-boot:run

# O desde IntelliJ: botón Run en UniRedApplication.java
```

### 3.5 URLs útiles en desarrollo

| Servicio | URL |
|----------|-----|
| API local | `http://localhost:3009/api/v1` |
| Swagger UI | `http://localhost:3009/api/v1/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:3009/api/v1/v3/api-docs` |
| Health | `http://localhost:3009/api/v1/actuator/health` |

### 3.6 Migraciones de base de datos

| Migración | Descripción |
|-----------|-------------|
| `V1__create_core_tables.sql` | Tablas: usuarios, sesiones, actividades, inscripciones, mentores, mentor_materias, solicitudes_mentoria, recordatorios, configuracion_notificaciones, notificaciones, publicaciones_rrss |
| `V2__add_login_attempt_control.sql` | Columnas de protección contra fuerza bruta: `intentos_fallidos`, `bloqueado_hasta` |
| `V3__align_usuario_estudiante_columns.sql` | Columnas adicionales de estudiante: `correo_institucional`, `promedio_academico`, `estado_cuenta` |

### 3.7 Recomendación para producción

1. **Externalizar toda configuración sensible** en secret manager (AWS Secrets Manager, HashiCorp Vault, etc.)
2. **Desplegar con imagen OCI** (Spring Boot Maven plugin) o contenedor Docker
3. **TLS extremo a extremo** con certificado válido
4. **Observabilidad centralizada**: logs estructurados, métricas Prometheus, trazas distribuidas
5. **Pipeline CI/CD** con pruebas unitarias, integración y escaneo de seguridad (SAST/DAST)
6. **Base de datos en producción** con backups automáticos y replicación
7. **WAF y rate limiting** a nivel de gateway/reverse proxy

---

## 4) Instrucción de uso de APIs

### 4.1 Base URL

Todas las rutas van bajo el prefijo global: **`/api/v1`**

### 4.2 Flujo de autenticación

1. **Login**: `POST /auth/login` con correo institucional + password -> obtienes `accessToken` y `refreshToken`
2. **Requests privados**: enviar header `Authorization: Bearer <accessToken>`
3. **Token expirado**: `POST /auth/refresh` con refreshToken -> obtienes nuevo accessToken
4. **Logout**: `POST /auth/logout` con header Authorization -> invalida sesión

### 4.3 Formato estándar de respuesta

**Éxito:**
```json
{
  "success": true,
  "message": "Texto descriptivo",
  "data": {},
  "timestamp": "2026-04-05T21:00:00",
  "correlationId": "uuid"
}
```

**Error:**
```json
{
  "success": false,
  "message": "Descripción del error",
  "data": {
    "status": 400,
    "error": "Validation Error",
    "validationErrors": {
      "correo": "Solo se permiten correos @uniminuto.edu.co"
    }
  },
  "timestamp": "2026-04-05T21:05:00",
  "correlationId": "uuid"
}
```

### 4.4 Códigos HTTP de error

| Código | Significado | Causa |
|--------|-------------|-------|
| `400` | Bad Request | Validaciones DTO fallidas o dominio no permitido |
| `401` | Unauthorized | Credenciales o token inválidos |
| `403` | Forbidden | Sin permisos de rol para la acción |
| `404` | Not Found | Recurso no encontrado |
| `409` | Conflict | Sin cupos, mentor sin capacidad |
| `422` | Unprocessable Entity | Cancelación fuera de plazo |
| `423` | Locked | Cuenta bloqueada por intentos fallidos |
| `500` | Internal Server Error | Error interno no controlado |

### 4.5 Catálogo completo de APIs

> **Prefijo global:** todas las rutas reales inician con `/api/v1`.

---

#### Auth

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| `POST` | `/auth/login` | Público | Iniciar sesión con correo + password |
| `POST` | `/auth/refresh` | Público | Renovar token de acceso |
| `POST` | `/auth/logout` | Autenticado | Cerrar sesión |
| `PUT` | `/auth/fcm-token` | Autenticado | Actualizar token FCM para push notifications |

**Request Login:**
```json
{
  "correo": "estudiante@uniminuto.edu.co",
  "password": "Password123"
}
```

**Response Login:**
```json
{
  "success": true,
  "message": "Inicio de sesión exitoso",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 1800,
    "usuario": {
      "id": 1,
      "primerNombre": "Juan",
      "primerApellido": "Perez",
      "correo": "estudiante@uniminuto.edu.co",
      "rol": "ESTUDIANTE",
      "programaAcademico": "Ingeniería de Sistemas",
      "semestre": 5
    }
  },
  "timestamp": "2026-04-05T21:00:00",
  "correlationId": "uuid"
}
```

---

#### Actividades

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| `GET` | `/actividades` | ESTUDIANTE | Listar actividades con filtros y paginación |
| `GET` | `/actividades/{id}` | ESTUDIANTE | Obtener detalle de actividad |
| `POST` | `/actividades` | ADMINISTRADOR | Crear actividad |
| `PUT` | `/actividades/{id}` | ADMINISTRADOR | Actualizar actividad |
| `DELETE` | `/actividades/{id}` | ADMINISTRADOR | Eliminar actividad (lógico) |
| `POST` | `/actividades/{id}/inscribir` | ESTUDIANTE | Inscribirse en actividad |
| `DELETE` | `/actividades/{id}/cancelar` | ESTUDIANTE | Cancelar inscripción (máx. 2h antes) |
| `POST` | `/actividades/{id}/asistencia/{estudianteId}` | ADMINISTRADOR | Registrar asistencia |

**Query params GET /actividades:**
- `categoria`: `CULTURAL` | `DEPORTES` | `APOYO` | `SALUD`
- `fecha`: `yyyy-MM-dd`
- `recordatorioWa`: `true` | `false`
- `page` (default 0)
- `size` (default 20)

**Request Crear Actividad:**
```json
{
  "nombre": "Taller de IA",
  "lugar": "Auditorio A",
  "descripcion": "Introducción a IA",
  "fechaHora": "2026-05-15T14:00:00",
  "duracionMinutos": 120,
  "categoria": "APOYO",
  "cupoTotal": 80
}
```

---

#### Mentorías

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| `GET` | `/mentorias/mentores` | ESTUDIANTE | Recomendar mentores por compatibilidad |
| `GET` | `/mentorias/mentores/{id}` | ESTUDIANTE | Detalle de mentor |
| `POST` | `/mentorias/postular` | ESTUDIANTE | Postularse como mentor |
| `POST` | `/mentorias/solicitar` | ESTUDIANTE | Solicitar mentoría |
| `PUT` | `/mentorias/solicitudes/{id}/confirmar` | ESTUDIANTE (mentor) | Confirmar solicitud de mentoría |
| `PUT` | `/mentorias/solicitudes/{id}/rechazar` | ESTUDIANTE (mentor) | Rechazar solicitud de mentoría |
| `GET` | `/mentorias/mis-solicitudes` | ESTUDIANTE | Listar mis solicitudes |
| `PUT` | `/mentorias/mentores/{id}/aprobar` | ADMINISTRADOR | Aprobar mentor |
| `PUT` | `/mentorias/mentores/{id}/rechazar` | ADMINISTRADOR | Rechazar mentor |

**Request Postularse como Mentor:**
```json
{
  "materias": ["Cálculo", "Programación"],
  "disponibilidad": "[{\"dia\":\"LUNES\",\"inicio\":\"08:00\",\"fin\":\"10:00\"}]",
  "bio": "Me gusta apoyar a estudiantes de primer semestre"
}
```

**Request Solicitar Mentoría:**
```json
{
  "mentorId": 10,
  "motivacion": "Necesito refuerzo en estructuras de datos",
  "numeroWhatsapp": "+573001112233"
}
```

---

#### Notificaciones

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| `GET` | `/notificaciones` | Autenticado | Obtener notificaciones (máx. 20 visibles) |
| `PUT` | `/notificaciones/{id}/leer` | Autenticado | Marcar como leída |
| `PUT` | `/notificaciones/leer-todas` | Autenticado | Marcar todas como leídas |
| `GET` | `/notificaciones/config` | Autenticado | Obtener configuración de notificaciones |
| `PUT` | `/notificaciones/config` | Autenticado | Actualizar configuración |

**Request Configurar Notificaciones (parcial):**
```json
{
  "recibirTramites": true,
  "recibirActividades": true,
  "recibirMentoria": true,
  "recibirRrss": true,
  "recibirBot": true,
  "modificadoMentoria": false,
  "numeroWhatsapp": "+573001112233"
}
```

---

#### RRSS (Redes Sociales)

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| `GET` | `/rrss/feed` | Autenticado | Feed de publicaciones cacheadas |
| `POST` | `/rrss/sincronizar` | ADMINISTRADOR | Sincronizar feed manualmente |

**Query params GET /rrss/feed:**
- `red` (opcional)
- `hashtag` (opcional)
- `page` (default 0)
- `size` (default 20)

---

#### Perfil

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| `GET` | `/perfil` | Autenticado | Obtener perfil del usuario |
| `PUT` | `/perfil/telefono` | Autenticado | Actualizar teléfono |
| `PUT` | `/perfil/password` | Autenticado | Cambiar contraseña |
| `PUT` | `/perfil/foto` | Autenticado | Actualizar foto de perfil |
| `GET` | `/perfil/estadísticas` | Autenticado | Obtener estadísticas del perfil |
| `GET` | `/perfil/historial-asistencias` | Autenticado | Obtener historial de asistencias |

**Request Cambiar Contraseña:**
```json
{
  "passwordActual": "Actual123",
  "passwordNueva": "Nueva12345",
  "confirmacionPassword": "Nueva12345"
}
```

---

#### Dashboard

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| `GET` | `/dashboard` | ESTUDIANTE | Resumen principal del estudiante |

---

#### Admin

> Todos requieren `ROLE_ADMINISTRADOR`.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/admin/usuarios` | Crear estudiante |
| `GET` | `/admin/usuarios?page=0&size=20` | Listar estudiantes |
| `PUT` | `/admin/usuarios/{id}` | Actualizar estudiante (parcial) |
| `GET` | `/admin/actividades/{id}/asistentes` | Obtener asistentes de actividad |
| `POST` | `/admin/notificaciones/enviar` | Enviar notificación a usuario |

**Request Crear Estudiante:**
```json
{
  "tipoDocumento": "CC",
  "numeroDocumento": "123456789",
  "primerNombre": "Ana",
  "primerApellido": "Lopez",
  "correo": "ana.lopez@uniminuto.edu.co",
  "password": "Password123",
  "programaAcademico": "Ingeniería de Sistemas",
  "semestre": 4,
  "sede": "Zipaquirá"
}
```

**Request Enviar Notificación:**
```json
{
  "usuarioId": 10,
  "titulo": "Recordatorio",
  "mensaje": "No olvides completar tu trámite",
  "tipo": "TRAMITE",
  "prioridad": "MEDIA",
  "urlAccion": "unired://tramites/123"
}
```

---

## 5) Endpoints públicos y reservados

> Prefijo real: `/api/v1`

### 5.1 Públicos (sin autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/login` | Iniciar sesión |
| `POST` | `/auth/refresh` | Renovar token |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/swagger-ui/**` | Interfaz Swagger |
| `GET` | `/v3/api-docs/**` | Especificación OpenAPI |

### 5.2 Privados (cualquier usuario autenticado)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/logout` | Cerrar sesión |
| `PUT` | `/auth/fcm-token` | Actualizar token FCM |
| `GET` | `/perfil` | Obtener perfil |
| `PUT` | `/perfil/telefono` | Actualizar teléfono |
| `PUT` | `/perfil/password` | Cambiar contraseña |
| `PUT` | `/perfil/foto` | Actualizar foto |
| `GET` | `/perfil/estadísticas` | Estadísticas del perfil |
| `GET` | `/perfil/historial-asistencias` | Historial de asistencias |
| `GET` | `/notificaciones` | Obtener notificaciones |
| `PUT` | `/notificaciones/{id}/leer` | Marcar como leída |
| `PUT` | `/notificaciones/leer-todas` | Marcar todas como leídas |
| `GET` | `/notificaciones/config` | Obtener configuración |
| `PUT` | `/notificaciones/config` | Actualizar configuración |
| `GET` | `/rrss/feed` | Feed de publicaciones |

### 5.3 Reservados a `ROLE_ESTUDIANTE`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/dashboard` | Dashboard del estudiante |
| `GET` | `/actividades` | Listar actividades |
| `GET` | `/actividades/{id}` | Detalle de actividad |
| `POST` | `/actividades/{id}/inscribir` | Inscribirse |
| `DELETE` | `/actividades/{id}/cancelar` | Cancelar inscripción |
| `GET` | `/mentorias/mentores` | Recomendar mentores |
| `GET` | `/mentorias/mentores/{id}` | Detalle de mentor |
| `POST` | `/mentorias/postular` | Postularse como mentor |
| `POST` | `/mentorias/solicitar` | Solicitar mentoría |
| `PUT` | `/mentorias/solicitudes/{id}/confirmar` | Confirmar solicitud |
| `PUT` | `/mentorias/solicitudes/{id}/rechazar` | Rechazar solicitud |
| `GET` | `/mentorias/mis-solicitudes` | Mis solicitudes |

### 5.4 Reservados a `ROLE_ADMINISTRADOR`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/admin/usuarios` | Crear estudiante |
| `GET` | `/admin/usuarios` | Listar estudiantes |
| `PUT` | `/admin/usuarios/{id}` | Actualizar estudiante |
| `GET` | `/admin/actividades/{id}/asistentes` | Asistentes de actividad |
| `POST` | `/admin/notificaciones/enviar` | Enviar notificación |
| `POST` | `/rrss/sincronizar` | Sincronizar RRSS |
| `POST` | `/actividades` | Crear actividad |
| `PUT` | `/actividades/{id}` | Actualizar actividad |
| `DELETE` | `/actividades/{id}` | Eliminar actividad |
| `POST` | `/actividades/{id}/asistencia/{estudianteId}` | Registrar asistencia |
| `PUT` | `/mentorias/mentores/{id}/aprobar` | Aprobar mentor |
| `PUT` | `/mentorias/mentores/{id}/rechazar` | Rechazar mentor |

---

## 6) Estado actual de registro de usuarios

### 6.1 Situación actual

- **No existe endpoint público de registro autoconsumo** para estudiantes.
- El alta actual de estudiante está en flujo administrativo:
  `POST /admin/usuarios` (requiere `ROLE_ADMINISTRADOR`).
- No hay mecanismo para que un estudiante se registre de forma autónoma.

### 6.2 Registro planificado (en desarrollo)

Se está implementando un flujo de registro en 3 pasos con verificación por email:

1. **`POST /auth/register/solicitar`** — El estudiante ingresa su correo institucional, se envía código de 6 dígitos por email
2. **`POST /auth/register/verificar`** — Valida el código recibido (máx. 3 intentos, expira en 10 min)
3. **`POST /auth/register/completar`** — Con código verificado, el estudiante completa todos sus datos de registro

**Nuevos campos en modelo Usuario:**
- `segundo_nombre` (VARCHAR 80)
- `segundo_apellido` (VARCHAR 80)

**Nueva tabla:**
- `codigos_verificacion` — almacena códigos hashados con expiración, intentos y estado de verificación

**Configuración de email:**
- Servicio SMTP vía Gmail (App Password)
- Variables: `EMAIL_USERNAME`, `EMAIL_PASSWORD`

---

## 7) Fuentes técnicas revisadas

| Archivo | Descripción |
|---------|-------------|
| `src/main/java/com/unired/config/SecurityConfig.java` | Configuración de Spring Security, cadena de filtros, permitAll |
| `src/main/java/com/unired/infrastructure/security/JwtAuthFilter.java` | Filtro JWT, validación de token, expiración deslizante |
| `src/main/java/com/unired/infrastructure/security/JwtUtil.java` | Generación y validación de JWT (HS512) |
| `src/main/java/com/unired/infrastructure/security/UserDetailsServiceImpl.java` | Carga de UserDetails desde DB, construcción de authorities |
| `src/main/java/com/unired/infrastructure/security/AppUserDetails.java` | Implementación de UserDetails con campos custom |
| `src/main/java/com/unired/application/service/AuthService.java` | Lógica de login, refresh, logout, bloqueo por intentos |
| `src/main/java/com/unired/exception/GlobalExceptionHandler.java` | Manejo global de excepciones, formato de error estándar |
| `src/main/resources/application.yml` | Configuración principal de la aplicación |
| `src/main/resources/db/migration/V1__create_core_tables.sql` | Esquema inicial de base de datos |
| `src/main/resources/db/migration/V2__add_login_attempt_control.sql` | Columnas de protección contra fuerza bruta |
| `src/main/resources/db/migration/V3__align_usuario_estudiante_columns.sql` | Columnas adicionales de estudiante |
| `ANDROID_INTEGRACION_BACKEND_UNIRED.md` | Guía de integración para Android |
| `pom.xml` | Dependencias y configuración de build |

---

*Documento generado a partir del análisis del código fuente del proyecto UniRed Backend.*
*Fecha de generación: 2026-04-05*
