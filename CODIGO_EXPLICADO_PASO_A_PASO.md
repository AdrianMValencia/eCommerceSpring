# Codigo Explicado Paso A Paso (PayPal + JWT + Roles)

Guia de presentacion para explicar la implementacion directamente sobre el codigo fuente.

## 1. Configuracion Base

### 1.1 Properties de desarrollo
Archivo: `src/main/resources/application-dev.properties`

Que explicar:
1. Conexion a base de datos.
2. Configuracion PayPal sandbox (`paypal.base-url`, `client-id`, `client-secret`).
3. `paypal.webhook-id`.
4. `paypal.return-url` y `paypal.cancel-url` hacia frontend.
5. `paypal.currency=USD` para evitar `CURRENCY_NOT_SUPPORTED`.
6. JWT (`jwt.secret`, `jwt.expiration-ms`).

Tip de presentacion:
- Menciona que ngrok se usa solo para webhook en local.

## 2. Modelo De Dominio De Pagos

### 2.1 Estados de orden
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/domain/models/OrderState.java`

Que explicar:
1. Nuevos estados: `CREATED`, `PENDING_PAYMENT`, `PAID`, `PAYMENT_FAILED`.
2. Estados legado: `CANCELLED`, `CONFIRMED`.

### 2.2 Datos de pago en orden
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/domain/models/Order.java`

Que explicar:
1. Campos de trazabilidad: `paypalOrderId`, `paypalCaptureId`.
2. Campos de negocio: `paymentProvider`, `paymentStatus`, `paidAt`.

## 3. Persistencia Y Mapeo

### 3.1 Entidad de orden
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/persistence/entities/OrderEntity.java`

Que explicar:
1. Nuevas columnas de pago.
2. `paypalOrderId` con `unique=true`.

### 3.2 Mapper de orden
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/mappers/OrderMapper.java`

Que explicar:
1. Mapeo bidireccional de campos nuevos entre entidad y dominio.

### 3.3 Lookup por paypalOrderId
Archivos:
- `src/main/java/com/amechato/ecommerce_api/backend/domain/ports/IOrderRepository.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/adapters/IOrderCrudRepository.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/adapters/OrderCrudRepositoryImpl.java`
- `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/OrderService.java`

Que explicar:
1. Se agrega busqueda por `paypalOrderId` para webhook/captura.

## 4. Integracion PayPal

### 4.1 Cliente PayPal
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/paypal/PayPalApiClient.java`

Que explicar por metodos:
1. `getAccessToken()`: OAuth2 client credentials.
2. `createOrder(...)`: crea orden de pago en PayPal.
3. `captureOrder(...)`: captura orden aprobada.
4. `verifyWebhookSignature(...)`: valida firma webhook con PayPal.

### 4.2 Caso de uso PayPal
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/PayPalCheckoutService.java`

Que explicar por flujo:
1. `createPayment(orderId)`:
- valida orden local
- fuerza 2 decimales (`DECIMAL_PRECISION` fix)
- crea orden PayPal
- guarda `paypalOrderId`
- devuelve `approvalUrl`

2. `capturePayment(orderId, paypalOrderId)`:
- valida correlacion local/paypal
- captura en PayPal
- guarda `captureId`
- cambia estado a `PAID` o `PAYMENT_FAILED`

3. `processWebhook(payload, headers)`:
- verifica firma
- procesa eventos (`APPROVED`, `CAPTURE.COMPLETED`, etc.)
- actualiza estado local

### 4.3 Controller PayPal
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/PayPalPaymentController.java`

Endpoints:
1. `POST /api/payments/paypal/create`
2. `POST /api/payments/paypal/capture`
3. `POST /api/payments/paypal/webhook`

### 4.4 Flujo con ngrok
Que explicar:
1. Levantar backend en 8080.
2. Ejecutar `ngrok http 8080`.
3. Configurar webhook PayPal con URL publica:
`https://<dominio-ngrok>/api/payments/paypal/webhook`

## 5. JWT + Login

### 5.1 Dependencias
Archivo: `pom.xml`

Que explicar:
1. `spring-security-crypto` para BCrypt.
2. `spring-boot-starter-security` para autorización.
3. `jjwt-*` para tokens.

### 5.2 Configuracion JWT
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/JwtProperties.java`

Que explicar:
1. `secret` y `expirationMs` parametrizados por properties.

### 5.3 Servicio JWT
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/security/JwtService.java`

Que explicar:
1. `generateToken(...)` agrega claims `userId` y `role`.
2. `extractClaims(...)` y `isTokenValid(...)`.
3. Validacion de secreto (minimo 32 caracteres).

### 5.4 AuthService
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/AuthService.java`

Que explicar:
1. Validacion de input login.
2. Verificacion BCrypt (`matches`).
3. Retorno de `token`, `email`, `userType`.

### 5.5 AuthController
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/AuthController.java`

Endpoint:
1. `POST /api/auth/login`

Request:
```json
{
  "email": "john@demo.com",
  "password": "123456"
}
```

Response:
```json
{
  "token": "<JWT>",
  "userId": 1,
  "email": "john@demo.com",
  "userType": "USER"
}
```

## 6. Encriptacion De Password En Registro

### 6.1 UserService
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/UserService.java`

Que explicar:
1. En `save(user)` se encripta password si viene en texto plano.
2. Si no llega rol, asigna `USER`.

### 6.2 UserController
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/UserController.java`

Que explicar:
1. Se sanitiza respuesta para no exponer password (`password = null`).

## 7. Reglas De Acceso Por Rol

### 7.1 SecurityConfiguration
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/SecurityConfiguration.java`

Que explicar:
1. `POST /api/auth/login` -> publico.
2. `POST /api/users` -> publico (registro).
3. `/api/orders/**` -> `ADMIN`.
4. `/api/payments/paypal/**` -> `ADMIN`.
5. Resto -> autenticado (`USER` o `ADMIN`).

### 7.2 JwtAuthenticationFilter
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/security/JwtAuthenticationFilter.java`

Que explicar:
1. Lee `Authorization: Bearer`.
2. Valida token.
3. Extrae `role` claim y crea `ROLE_<role>`.
4. Carga contexto de seguridad.

## 8. Errores Controlados

### 8.1 Global handler
Archivo: `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/GlobalExceptionHandler.java`

Que explicar:
1. `AUTH_ERROR` -> 401
2. `PAYPAL_API_ERROR` -> 502
3. `WEBHOOK_VERIFICATION_ERROR` -> 401
4. Otros codigos de negocio.

## 9. Demo Recomendada (Orden De Presentacion)

1. Mostrar `application-dev.properties` (PayPal + JWT + ngrok).
2. Crear usuario y mostrar que password en BD queda hash.
3. Hacer login y mostrar token + userType.
4. Mostrar reglas de rol en `SecurityConfiguration`.
5. Consumir endpoint USER permitido (`GET /products`).
6. Probar endpoint ADMIN con token USER y mostrar 403.
7. Hacer flujo PayPal completo con orden ADMIN:
- create order
- create paypal
- approve
- capture
- webhook

## 10. Cierre Tecnico

Mensaje final sugerido:

"La solucion queda desacoplada por capas, con trazabilidad completa de pago, seguridad basada en JWT, hash BCrypt para credenciales, reglas por rol en endpoints criticos y webhook verificado para confiabilidad en transacciones PayPal."