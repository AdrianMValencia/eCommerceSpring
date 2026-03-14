# Implementacion JWT + Encriptacion De Password (Paso A Paso)

Este documento detalla, de inicio a fin, como quedo implementado el login con JWT y la encriptacion de contrasena en el backend.

## 1. Objetivo

1. Encriptar la contrasena al crear usuario.
2. Crear endpoint de login con JWT.
3. Devolver en login: `token` + rol del usuario (`userType`).
4. Documentar contratos y flujo para frontend.

## 2. Dependencias Agregadas

En `pom.xml` se agregaron:

1. `spring-security-crypto` (BCrypt).
2. `jjwt-api`.
3. `jjwt-impl` (runtime).
4. `jjwt-jackson` (runtime).

Estas dependencias permiten hash seguro de password y generacion de JWT firmados.

## 3. Configuracion JWT

### 3.1 Clase De Properties

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/JwtProperties.java`

Campos:

1. `jwt.secret`
2. `jwt.expiration-ms`

### 3.2 Properties En Ambientes

Archivo dev:
`src/main/resources/application-dev.properties`

```properties
jwt.secret=dev-super-secret-key-change-this-to-at-least-32-chars
jwt.expiration-ms=86400000
```

Archivo prd:
`src/main/resources/application-prd.properties`

```properties
jwt.secret=${JWT_SECRET:}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
```

## 4. Bean De PasswordEncoder

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/BeanConfiguration.java`

Se agrego:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Con esto se habilita hash BCrypt para usuarios nuevos.

## 5. Encriptacion En Creacion De Usuario

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/application/usecases/UserService.java`

Logica implementada en `save(User user)`:

1. Valida que `user` no sea null.
2. Normaliza email con `trim()`.
3. Asigna `UserType.USER` por defecto si no llega.
4. Valida que `password` no sea vacia.
5. Encripta password con BCrypt si no esta ya hasheada (`$2a$`, `$2b$`, `$2y$`).

Resultado: cualquier usuario nuevo queda con password encriptada en BD.

## 6. Servicio JWT

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/security/JwtService.java`

Responsabilidad:

1. Generar token firmado HMAC.
2. Incluir claims utiles:
   - `sub`: email del usuario
   - `userId`
   - `role` (valor de `UserType`)
3. Respetar expiracion configurada en properties.

Validacion aplicada:

- `jwt.secret` debe tener al menos 32 caracteres.

## 7. Servicio De Autenticacion

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/application/usecases/AuthService.java`

Flujo de `login(email, password)`:

1. Valida email/password obligatorios.
2. Busca usuario por email.
3. Verifica password con `passwordEncoder.matches(...)`.
4. Si credenciales invalidas -> `AuthException`.
5. Si todo ok -> genera JWT y devuelve respuesta de login.

## 8. DTOs De Login

Archivos:

1. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/auth/LoginRequest.java`
2. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/auth/LoginResponse.java`

### LoginRequest

```json
{
  "email": "user@mail.com",
  "password": "123456"
}
```

### LoginResponse

```json
{
  "token": "<jwt>",
  "userId": 1,
  "email": "user@mail.com",
  "userType": "USER"
}
```

## 9. Endpoint De Login

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/AuthController.java`

Endpoint implementado:

- `POST /api/auth/login`

Consume `LoginRequest` y devuelve `LoginResponse`.

## 10. Seguridad De Respuesta En Usuarios

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/UserController.java`

Se agrego sanitizacion para no devolver password en:

1. `POST /api/users`
2. `GET /api/users/{id}`
3. `GET /api/users/by-email`

La respuesta ahora retorna `password = null`.

## 11. Manejo De Errores De Autenticacion

Archivo:
`src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/GlobalExceptionHandler.java`

Se agrego manejo de `AuthException`:

- Codigo HTTP: `401`
- Codigo API: `AUTH_ERROR`

Ejemplo:

```json
{
  "code": "AUTH_ERROR",
  "message": "Invalid credentials",
  "timestamp": "2026-03-14T10:00:00Z",
  "path": "uri=/api/auth/login"
}
```

## 12. Prueba Manual Paso A Paso

### 12.1 Crear Usuario

`POST /api/users`

```json
{
  "username": "jdoe",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "999999999",
  "userType": "USER"
}
```

Validar en BD que `password` este hasheada (empieza con `$2`).

### 12.2 Login

`POST /api/auth/login`

```json
{
  "email": "john@demo.com",
  "password": "123456"
}
```

Esperar respuesta con:

1. `token` (JWT)
2. `userType` (`USER` o `ADMIN`)

### 12.3 Login Con Error

Enviar password incorrecta y verificar:

- HTTP `401`
- `code = AUTH_ERROR`

## 13. Contrato Para Frontend

Frontend debe guardar:

1. `token` (Bearer token).
2. `userType` para control de UI/autorizacion.

Header recomendado para futuros endpoints protegidos:

```http
Authorization: Bearer <token>
```

## 14. Archivos Tocados En Esta Implementacion

1. `pom.xml`
2. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/BeanConfiguration.java`
3. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/JwtProperties.java`
4. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/security/JwtService.java`
5. `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/UserService.java`
6. `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/AuthService.java`
7. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/AuthController.java`
8. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/UserController.java`
9. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/auth/LoginRequest.java`
10. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/auth/LoginResponse.java`
11. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/AuthException.java`
12. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/GlobalExceptionHandler.java`
13. `src/main/resources/application-dev.properties`
14. `src/main/resources/application-prd.properties`

## 15. Recomendaciones De Produccion

1. No usar secretos hardcodeados en `application-dev.properties` para entornos compartidos.
2. En produccion, usar variables de entorno para `JWT_SECRET`.
3. Rotar secret periodicamente.
4. Reducir expiracion del token si el riesgo lo requiere.
5. Agregar refresh token si necesitas sesiones largas.
6. Proteger endpoints con filtro JWT (siguiente fase).
