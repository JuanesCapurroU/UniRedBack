# UniRed Backend API - Guia completa para Android Studio

Este documento describe **todo lo necesario** para conectar el front mobile (Android + Kotlin) con este backend Spring Boot.

Incluye:
- Como levantar el backend en local (usando Supabase Postgres remoto)
- URLs base para emulador y dispositivo fisico
- Flujo de autenticacion JWT
- Catalogo completo de endpoints
- Contratos request/response
- Ejemplos de implementacion con Retrofit + OkHttp
- Manejo de errores y troubleshooting

---

## 1) Resumen tecnico actual del backend

- Framework: Spring Boot 3.x
- Lenguaje: Java 21
- DB: Supabase Postgres (pooler)
- Auth: JWT (access + refresh) + Spring Security
- Migraciones: Flyway
- Docs API: Swagger/OpenAPI (SpringDoc)
- Context path global: `/api/v1`
- Wrapper estandar de respuesta: `ApiResponse<T>`

---

## 2) Levantar backend local (con DB Supabase)

Aunque la BD esta en Supabase, el backend corre local en tu maquina.

### Variables de entorno minimas

Opcion recomendada: crear/usar el archivo `.env` en la raiz de `Backend/` (ya incluido en este repo) con:

```dotenv
DB_PASSWORD=tu_password_supabase
JWT_SECRET=tu_secret_base64
FIREBASE_ENABLED=false
FIREBASE_CREDENTIALS_PATH=classpath:firebase-service-account.json
```

Alternativa en PowerShell:

```powershell
$env:DB_PASSWORD="IXfVOGuUviWdJwND"
$env:JWT_SECRET="<BASE64_SECRET_MIN_512_BITS>"
```

Opcional para FCM:

```powershell
$env:FIREBASE_ENABLED="true"
$env:FIREBASE_CREDENTIALS_PATH="classpath:firebase-service-account.json"
```

### Ejecutar

```powershell
.\mvnw spring-boot:run
```

### URLs utiles

- API local: `http://localhost:3009/api/v1`
- Swagger UI: `http://localhost:3009/api/v1/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:3009/api/v1/v3/api-docs`
- Health: `http://localhost:3009/api/v1/actuator/health`

---

## 3) Base URL para Android (local)

### Emulador Android Studio

Usa:

```text
http://10.0.2.2:3009/api/v1/
```

### Genymotion

```text
http://10.0.3.2:3009/api/v1/
```

### Dispositivo fisico en la misma red WiFi

Usa la IP LAN de tu PC, por ejemplo:

```text
http://192.168.1.20:3009/api/v1/
```

Y habilita firewall para el puerto `3009`.

---

## 4) Seguridad y autenticacion

## 4.1 Endpoints publicos

Solo estos endpoints son publicos:
- `POST /auth/login`
- `POST /auth/refresh`
- Swagger/OpenAPI
- Health

Todo lo demas requiere token JWT valido.

## 4.2 Flujo recomendado en Android

1. Login con correo institucional + password.
2. Guardar `accessToken`, `refreshToken`, `expiresIn`.
3. En cada request privado enviar header:

```http
Authorization: Bearer <accessToken>
```

4. Si recibes `401`, llamar `POST /auth/refresh`.
5. Reintentar request original con nuevo `accessToken`.
6. En logout llamar `POST /auth/logout` y limpiar sesion local.

## 4.3 Reglas clave implementadas

- Solo correos `@uniminuto.edu.co` pueden hacer login.
- Password valida con BCrypt (strength 12).
- Bloqueo por intentos fallidos: 5 intentos -> bloqueo 15 minutos.
- Access token: 30 min.
- Refresh token: 7 dias.
- Sesion en DB con expiracion deslizante (cada request autentico renueva 30 min).

---

## 5) Formato estandar de respuestas

Todas las respuestas REST usan este wrapper:

```json
{
  "success": true,
  "message": "Texto",
  "data": {},
  "timestamp": "2026-04-05T21:00:00",
  "correlationId": "uuid"
}
```

En error (`success=false`) `data` suele ser `ErrorDetail`:

```json
{
  "success": false,
  "message": "Solicitud invalida",
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

---

## 6) Codigos HTTP de error (GlobalExceptionHandler)

- `400` -> validaciones DTO o dominio no permitido
- `401` -> credenciales/token invalidos
- `403` -> sin permisos de rol
- `404` -> recurso no encontrado
- `409` -> conflicto de negocio (sin cupos, mentor sin capacidad)
- `422` -> cancelacion fuera de plazo
- `423` -> cuenta bloqueada
- `500` -> error interno

---

## 7) Catalogo completo de APIs

> **Prefijo global:** todas las rutas reales inician con `/api/v1`.

---

### 7.1 Auth

#### POST `/auth/login` (Publico)

Request:

```json
{
  "correo": "estudiante@uniminuto.edu.co",
  "password": "Password123"
}
```

Respuesta `LoginResponse`:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 1800,
  "usuario": {
    "id": 1,
    "primerNombre": "Juan",
    "primerApellido": "Perez",
    "correo": "estudiante@uniminuto.edu.co",
    "rol": "ESTUDIANTE",
    "programaAcademico": "Ingenieria de Sistemas",
    "semestre": 5
  }
}
```

#### POST `/auth/refresh` (Publico)

Request:

```json
{
  "refreshToken": "..."
}
```

Retorna nuevo `accessToken`.

#### POST `/auth/logout` (Autenticado)

Header:

```http
Authorization: Bearer <accessToken>
```

Sin body.

#### PUT `/auth/fcm-token` (Autenticado)

Request:

```json
{
  "fcmToken": "token_fcm"
}
```

---

### 7.2 Actividades

#### GET `/actividades` (Rol: ESTUDIANTE)

Query params opcionales:
- `categoria`: `CULTURAL | DEPORTES | APOYO | SALUD`
- `fecha`: `yyyy-MM-dd`
- `recordatorioWa`: `true|false`
- `page` (default 0)
- `size` (default 20)

Retorna `Page<ActividadResponse>` dentro de `ApiResponse.data`.

#### GET `/actividades/{id}` (Rol: ESTUDIANTE)

Retorna `ActividadResponse`.

#### POST `/actividades` (Rol: ADMINISTRADOR)

Request:

```json
{
  "nombre": "Taller de IA",
  "lugar": "Auditorio A",
  "descripcion": "Introduccion a IA",
  "fechaHora": "2026-05-15T14:00:00",
  "duracionMinutos": 120,
  "categoria": "APOYO",
  "cupoTotal": 80
}
```

#### PUT `/actividades/{id}` (Rol: ADMINISTRADOR)

Mismo body que crear.

#### DELETE `/actividades/{id}` (Rol: ADMINISTRADOR)

Elimina logico (`activa=false`).

#### POST `/actividades/{id}/inscribir` (Rol: ESTUDIANTE)

Sin body. Retorna `InscripcionResponse`.

Regla: si no hay cupos -> `409`.

#### DELETE `/actividades/{id}/cancelar` (Rol: ESTUDIANTE)

Sin body.

Regla: si faltan menos de 2 horas -> `422`.

#### POST `/actividades/{id}/asistencia/{estudianteId}` (Rol: ADMINISTRADOR)

Marca asistencia.

---

### 7.3 Mentorias

#### GET `/mentorias/mentores` (Rol: ESTUDIANTE)

Retorna lista `MentorResponse` ordenada por compatibilidad.

#### GET `/mentorias/mentores/{id}` (Rol: ESTUDIANTE)

Retorna `MentorDetalleResponse`.

#### POST `/mentorias/postular` (Rol: ESTUDIANTE)

Request:

```json
{
  "materias": ["Calculo", "Programacion"],
  "disponibilidad": "[{\"dia\":\"LUNES\",\"inicio\":\"08:00\",\"fin\":\"10:00\"}]",
  "bio": "Me gusta apoyar a estudiantes de primer semestre"
}
```

#### POST `/mentorias/solicitar` (Rol: ESTUDIANTE)

Request:

```json
{
  "mentorId": 10,
  "motivacion": "Necesito refuerzo en estructuras de datos",
  "numeroWhatsapp": "+573001112233"
}
```

#### PUT `/mentorias/solicitudes/{id}/confirmar` (Rol: ESTUDIANTE - mentor)

Sin body.

#### PUT `/mentorias/solicitudes/{id}/rechazar` (Rol: ESTUDIANTE - mentor)

Sin body.

#### GET `/mentorias/mis-solicitudes` (Rol: ESTUDIANTE)

Retorna lista `SolicitudResponse`.

#### PUT `/mentorias/mentores/{id}/aprobar` (Rol: ADMINISTRADOR)

#### PUT `/mentorias/mentores/{id}/rechazar` (Rol: ADMINISTRADOR)

---

### 7.4 Notificaciones

#### GET `/notificaciones` (Autenticado)

Retorna lista `NotificacionResponse`.

Regla: maximo 20 visibles por usuario.

#### PUT `/notificaciones/{id}/leer` (Autenticado)

Marca una como leida.

#### PUT `/notificaciones/leer-todas` (Autenticado)

Marca todas como leidas.

#### GET `/notificaciones/config` (Autenticado)

Retorna `ConfiguracionNotificacionesResponse`.

#### PUT `/notificaciones/config` (Autenticado)

Request parcial permitido:

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

### 7.5 RRSS

#### GET `/rrss/feed` (Autenticado)

Query params:
- `red` (opcional)
- `hashtag` (opcional)
- `page` (default 0)
- `size` (default 20)

Retorna `Page<PublicacionRRSSResponse>`.

#### POST `/rrss/sincronizar` (Rol: ADMINISTRADOR)

Sin body.

---

### 7.6 Perfil

#### GET `/perfil` (Autenticado)

Retorna `PerfilResponse`.

#### PUT `/perfil/telefono` (Autenticado)

```json
{ "telefono": "+573001112233" }
```

#### PUT `/perfil/password` (Autenticado)

```json
{
  "passwordActual": "Actual123",
  "passwordNueva": "Nueva12345",
  "confirmacionPassword": "Nueva12345"
}
```

#### PUT `/perfil/foto` (Autenticado)

```json
{ "fotoUrl": "https://..." }
```

#### GET `/perfil/estadisticas` (Autenticado)

Retorna `EstadisticasResponse`.

#### GET `/perfil/historial-asistencias` (Autenticado)

Retorna `List<InscripcionResponse>`.

---

### 7.7 Dashboard

#### GET `/dashboard` (Rol: ESTUDIANTE)

Retorna `DashboardResponse`.

---

### 7.8 Admin

> Todos requieren `ROLE_ADMINISTRADOR`.

#### POST `/admin/usuarios`

`CrearEstudianteRequest`:

```json
{
  "tipoDocumento": "CC",
  "numeroDocumento": "123456789",
  "primerNombre": "Ana",
  "primerApellido": "Lopez",
  "correo": "ana.lopez@uniminuto.edu.co",
  "password": "Password123",
  "programaAcademico": "Ingenieria de Sistemas",
  "semestre": 4,
  "sede": "Zipaquira"
}
```

#### GET `/admin/usuarios?page=0&size=20`

Retorna `Page<EstudianteResponse>`.

#### PUT `/admin/usuarios/{id}`

`ActualizarEstudianteRequest` (parcial):

```json
{
  "primerNombre": "Ana Maria",
  "telefono": "3000000000",
  "correo": "ana.lopez@uniminuto.edu.co",
  "programaAcademico": "Ingenieria de Software",
  "semestre": 5,
  "sede": "Zipaquira",
  "activo": true
}
```

#### GET `/admin/actividades/{id}/asistentes`

Retorna `List<EstudianteResponse>`.

#### POST `/admin/notificaciones/enviar`

```json
{
  "usuarioId": 10,
  "titulo": "Recordatorio",
  "mensaje": "No olvides completar tu tramite",
  "tipo": "TRAMITE",
  "prioridad": "MEDIA",
  "urlAccion": "unired://tramites/123"
}
```

---

## 8) Contratos DTO (resumen de campos)

## 8.1 Request DTOs

- `LoginRequest`: `correo`, `password`
- `RefreshTokenRequest`: `refreshToken`
- `FcmTokenRequest`: `fcmToken`
- `ActividadRequest`: `nombre`, `lugar`, `descripcion`, `fechaHora`, `duracionMinutos`, `categoria`, `cupoTotal`
- `PostulacionMentorDTO`: `materias[]`, `disponibilidad`, `bio`
- `SolicitudMentoriaDTO`: `mentorId`, `motivacion`, `numeroWhatsapp`
- `ChangePasswordRequest`: `passwordActual`, `passwordNueva`, `confirmacionPassword`
- `TelefonoRequest`: `telefono`
- `FotoRequest`: `fotoUrl`
- `ConfiguracionNotificacionesRequest`: flags de recepcion + `numeroWhatsapp`
- `CrearEstudianteRequest`, `ActualizarEstudianteRequest`, `EnviarNotificacionRequest` para modulo admin

## 8.2 Response DTOs

- `LoginResponse`: `accessToken`, `refreshToken`, `expiresIn`, `usuario`
- `UsuarioBasicoResponse`: datos basicos de usuario autenticado
- `ActividadResponse`, `InscripcionResponse`
- `MentorResponse`, `MentorDetalleResponse`, `SolicitudResponse`
- `NotificacionResponse`, `ConfiguracionNotificacionesResponse`
- `PerfilResponse`, `EstadisticasResponse`, `DashboardResponse`
- `PublicacionRRSSResponse`

---

## 9) Implementacion recomendada en Android (Retrofit)

## 9.1 Dependencias sugeridas

- Retrofit
- OkHttp
- Gson o Moshi
- Coroutines
- DataStore/EncryptedSharedPreferences para tokens

## 9.2 Modelos base Kotlin

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val timestamp: String?,
    val correlationId: String?
)

data class ErrorDetail(
    val status: Int,
    val error: String,
    val validationErrors: Map<String, String>?
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean
)
```

## 9.3 API Service (ejemplo)

```kotlin
interface UniRedApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): ApiResponse<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @PUT("auth/fcm-token")
    suspend fun updateFcm(@Body body: FcmTokenRequest): ApiResponse<Unit>

    @GET("dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponse>

    @GET("perfil")
    suspend fun getPerfil(): ApiResponse<PerfilResponse>

    @GET("actividades")
    suspend fun getActividades(
        @Query("categoria") categoria: String? = null,
        @Query("fecha") fecha: String? = null,
        @Query("recordatorioWa") recordatorioWa: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<ActividadResponse>>
}
```

## 9.4 Retrofit Builder (emulador)

```kotlin
private const val BASE_URL = "http://10.0.2.2:3009/api/v1/"

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()
```

## 9.5 Auth Interceptor + Refresh

```kotlin
class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else chain.request()
        return chain.proceed(request)
    }
}
```

Usa un `Authenticator` o un mecanismo centralizado para:
- detectar `401`
- llamar `POST /auth/refresh`
- guardar nuevo `accessToken`
- reintentar la request

---

## 10) Cleartext HTTP en Android (solo desarrollo local)

Como la URL local es `http://`, en Android 9+ puede bloquearse si no habilitas cleartext para debug.

`AndroidManifest.xml` (debug):

```xml
<application
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config" />
```

`res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">192.168.1.20</domain>
    </domain-config>
</network-security-config>
```

En produccion, usar HTTPS.

---

## 11) Pruebas rapidas sugeridas

1. Abrir Swagger y probar `POST /auth/login`.
2. Copiar `accessToken` y autorizar en Swagger (`Authorize`).
3. Probar `GET /perfil`, `GET /dashboard`, `GET /actividades`.
4. Probar `POST /auth/refresh` cuando expire access token.
5. Validar errores de negocio:
   - login no institucional -> `400`
   - credenciales invalidas -> `401`
   - sin cupos en actividad -> `409`

---

## 12) Troubleshooting rapido

- `401 Unauthorized` en endpoints privados:
  - falta `Authorization: Bearer ...`
  - token expirado
  - sesion invalidada por logout

- `400` en login con correo valido visualmente:
  - debe cumplir regex exacta de correo institucional

- Android emulador no conecta:
  - usar `10.0.2.2`, no `localhost`

- Dispositivo fisico no conecta:
  - usar IP LAN del PC
  - revisar firewall

- Error de JWT secret:
  - `JWT_SECRET` debe estar definido y en Base64 con longitud suficiente

---

## 13) Nota operativa importante

Actualmente no hay endpoint publico para bootstrap de administrador.

Para usar rutas admin necesitas al menos un usuario con rol administrador en DB (`dtype=Administrador`) y password en BCrypt.

---

## 14) Referencias de implementacion en el backend

- Config general: `src/main/resources/application.yml`
- Seguridad: `src/main/java/com/unired/config/SecurityConfig.java`
- JWT filtro: `src/main/java/com/unired/infrastructure/security/JwtAuthFilter.java`
- Swagger: `src/main/java/com/unired/config/OpenApiConfig.java`
- Controladores: `src/main/java/com/unired/api/controller/`
- DTOs: `src/main/java/com/unired/application/dto/`
- Errores globales: `src/main/java/com/unired/exception/GlobalExceptionHandler.java`

---

Si quieres, en el siguiente paso te puedo generar:
- un `UniRedApi.kt` completo con **todos** los endpoints,
- todos los `data class` Kotlin,
- y un `AuthRepository` con refresh automatico ya listo para copiar/pegar.
