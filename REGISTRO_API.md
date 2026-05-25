# UniRed - Registro de Usuario

## Endpoints de Autenticación

### 1. Registrar Usuario

Registra un nuevo usuario y envía código de verificación al correo.

**Endpoint:** `POST /api/v1/auth/register`

**Encabezados:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "correo": "usuario@uniminuto.edu.co",
  "password": "miPassword123",
  "tipoDocumento": "CC",
  "numeroDocumento": "12345678",
  "primerNombre": "Juan",
  "primerApellido": "Perez",
  "segundoNombre": "Carlos",
  "segundoApellido": "Garcia",
  "telefono": "3001234567"
}
```

**Campos:**
| Campo | Requerido | Descripción |
|-------|----------|-------------|
| correo | Sí | Solo `@uniminuto.edu.co` |
| password | Sí | Mínimo 8 caracteres |
| tipoDocumento | Sí | CC, CE, TI, RC, PAS |
| numeroDocumento | Sí | Número de documento único |
| primerNombre | Sí | Primer nombre |
| primerApellido | Sí | Primer apellido |
| segundoNombre | No | Segundo nombre (opcional) |
| segundoApellido | No | Segundo apellido (opcional) |
| telefono | No | Teléfono (opcional) |

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Registro iniciado",
  "data": {
    "mensaje": "Código de verificación enviado a tu correo",
    "correo": "usuario@uniminuto.edu.co",
    "verificado": false
  }
}
```

---

### 2. Verificar Código

Verifica el código de 6 dígitos enviado al correo y retorna tokens de acceso.

**Endpoint:** `POST /api/v1/auth/verificar`

**Request Body:**
```json
{
  "correo": "usuario@uniminuto.edu.co",
  "codigo": "123456"
}
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Correo verificado exitosamente",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1800,
    "usuario": {
      "id": 1,
      "correo": "usuario@uniminuto.edu.co",
      "primerNombre": "Juan",
      "primerApellido": "Perez",
      "fotoUrl": null,
      "tipo": "Estudiante"
    }
  }
}
```

---

### 3. Reenviar Código

Reenvía el código de verificación si expiró o necesitas uno nuevo.

**Endpoint:** `POST /api/v1/auth/reenviar-codigo`

**Request Body:**
```json
{
  "correo": "usuario@uniminuto.edu.co"
}
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Código de verificación reenviado",
  "data": null
}
```

---

### 4. Iniciar Sesión

Inicia sesión con correo y contraseña (solo usuarios verificados).

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "correo": "usuario@uniminuto.edu.co",
  "password": "miPassword123"
}
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Inicio de sesión exitoso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1800,
    "usuario": { ... }
  }
}
```

---

### 5. Cerrar Sesión

Cierra la sesión actual.

**Endpoint:** `POST /api/v1/auth/logout`

**Encabezados:**
```
Authorization: Bearer <accessToken>
```

**Respuesta (200):**
```json
{
  "success": true,
  "message": "Sesión cerrada",
  "data": null
}
```

---

## Códigos de Error

| Código HTTP | Mensaje | Solución |
|-------------|---------|----------|
| 400 | "Solo se permiten correos @uniminuto.edu.co" | Usa un correo institucional |
| 400 | "El correo ya está registrado" | Usa otro correo o recupera contraseña |
| 400 | "El número de documento ya está registrado" | Usa otro documento |
| 400 | "El correo ya está verificado" | Ya completaste el registro |
| 400 | "Código inválido o expirado" | Verifica el código o solicita uno nuevo |
| 400 | "Demasiados intentos. Solicita un nuevo código en 15 minutos" | Espera 15 minutos |
| 401 | "Credenciales inválidas" | Verifica correo y contraseña |
| 401 | "Debes verificar tu correo antes de iniciar sesión" | Completa la verificación |

---

## Flujo de Registro

```
┌─────────────────┐
│  1. Registrar   │──── POST /register
│  (ingresar datos)│
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│  2. Recibir email   │
│  con código (6 dígitos)│
└────────┬────────────┘
         │
         ▼
┌─────────────────┐
│  3. Verificar  │──── POST /verificar
│  (enviar código)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  4. Login      │──── Token received
│  exitoso       │──── Puedes usar la app
└─────────────────┘
```

---

## Notas

- **Validez del código:** 15 minutos
- **Intentos máximos:** 3 por código
- **Dominio permitido:** Solo `@uniminuto.edu.co`
- **Token de acceso:** Expira en 30 minutos
- **Refresh token:** Expira en 7 días