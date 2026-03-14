# Runbook Completo: Integracion PayPal + Ngrok + Backend (Spring Boot)

Este documento es una guia operativa detallada para implementar, configurar, ejecutar y explicar el flujo de pagos con PayPal en este backend.

Objetivo: que puedas replicar y presentar el proceso paso a paso sin omitir ningun detalle.

## 1. Alcance

Se cubre:

1. Configuracion de credenciales de PayPal (Sandbox).
2. Configuracion de `application-dev.properties` y `application-prd.properties`.
3. Configuracion y uso de ngrok para webhooks en local.
4. Flujo funcional completo: crear orden, crear orden PayPal, aprobar, capturar, webhook.
5. Errores frecuentes y como resolverlos.
6. Checklist final de verificacion.

## 2. Prerrequisitos

1. Java instalado y funcional.
2. PostgreSQL levantado.
3. Backend compilando con `mvn -DskipTests compile`.
4. Cuenta en PayPal Developer (`https://developer.paypal.com`).
5. Ngrok instalado y funcional.

## 3. Arquitectura Del Flujo

Actores:

1. Frontend React.
2. Backend Spring Boot.
3. PayPal Checkout API.
4. PayPal Webhooks.
5. PostgreSQL.

Secuencia:

1. Front crea orden local en backend.
2. Backend crea orden de pago en PayPal.
3. Backend devuelve `approvalUrl`.
4. Front redirige al usuario a PayPal.
5. PayPal redirige al front (`success` o `cancel`).
6. Front llama backend para capturar.
7. Backend actualiza estado local.
8. Webhook confirma asincronamente.

## 4. Configuracion De PayPal Developer (Sandbox)

### 4.1 Crear App

1. Ir a `https://developer.paypal.com`.
2. Entrar a `Dashboard` -> `Apps & Credentials`.
3. En seccion `Sandbox`, crear app (`Create App`).
4. Tipo recomendado para e-commerce normal: `Merchant`.

### 4.2 Obtener Credenciales

Desde la app creada:

1. `Client ID` -> valor para `paypal.client-id`.
2. `Secret` (boton Show) -> valor para `paypal.client-secret`.

### 4.3 Crear Webhook

1. En la app, ir a `Webhooks` -> `Add webhook`.
2. Webhook URL (en local): `https://<dominio-ngrok>/api/payments/paypal/webhook`.
3. Seleccionar eventos:
   - `CHECKOUT.ORDER.APPROVED`
   - `PAYMENT.CAPTURE.COMPLETED`
   - `PAYMENT.CAPTURE.DENIED`
   - `PAYMENT.CAPTURE.DECLINED`
   - `PAYMENT.CAPTURE.REFUNDED`
4. Guardar y copiar `Webhook ID` (tipo `WH-...`).

## 5. Configuracion De Ngrok En Local

### 5.1 Iniciar Tunel

Con backend en puerto 8080:

```powershell
ngrok http 8080
```

### 5.2 Obtener Dominio Publico

En la salida de ngrok veras algo como:

`https://xxxxx.ngrok-free.dev`

Ese dominio se usa para el webhook.

### 5.3 URL Final De Webhook

```text
https://xxxxx.ngrok-free.dev/api/payments/paypal/webhook
```

Nota importante:

1. Si reinicias ngrok en plan free, el dominio puede cambiar.
2. Si cambia, actualiza la URL en PayPal y vuelve a guardar.

## 6. Configuracion Del Backend (Dev)

Archivo: `src/main/resources/application-dev.properties`

```properties
spring.application.name=backend

spring.datasource.url=jdbc:postgresql://localhost:5432/eCommerceDb
spring.datasource.username=postgres
spring.datasource.password=amechato

spring.jpa.hibernate.ddl-auto=update
spring.jpa.sql-show=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

paypal.base-url=https://api-m.sandbox.paypal.com
paypal.client-id=TU_CLIENT_ID_SANDBOX
paypal.client-secret=TU_CLIENT_SECRET_SANDBOX
paypal.webhook-id=TU_WEBHOOK_ID_WH
paypal.return-url=http://localhost:5173/checkout/paypal/success
paypal.cancel-url=http://localhost:5173/checkout/paypal/cancel
paypal.currency=USD
```

Regla clave:

- En Sandbox, usar `paypal.base-url=https://api-m.sandbox.paypal.com`.
- Para evitar `CURRENCY_NOT_SUPPORTED`, usar una moneda soportada por tu cuenta (en este proyecto funciono `USD`).

## 7. Configuracion Del Backend (Produccion)

Archivo: `src/main/resources/application-prd.properties`

```properties
spring.application.name=backend

paypal.base-url=${PAYPAL_BASE_URL:https://api-m.paypal.com}
paypal.client-id=${PAYPAL_CLIENT_ID:}
paypal.client-secret=${PAYPAL_CLIENT_SECRET:}
paypal.webhook-id=${PAYPAL_WEBHOOK_ID:}
paypal.return-url=${PAYPAL_RETURN_URL:https://app.example.com/checkout/paypal/success}
paypal.cancel-url=${PAYPAL_CANCEL_URL:https://app.example.com/checkout/paypal/cancel}
paypal.currency=${PAYPAL_CURRENCY:USD}
```

## 8. Endpoints Implementados

1. `POST /api/orders`
2. `POST /api/payments/paypal/create`
3. `POST /api/payments/paypal/capture`
4. `POST /api/payments/paypal/webhook`

## 9. Contratos Request/Response

### 9.1 Crear Orden Local

Request:

```json
{
  "userId": 2,
  "orderDetails": [
    {
      "productId": 10,
      "quantity": 2,
      "price": 100.00
    }
  ]
}
```

Respuesta esperada (ejemplo):

```json
{
  "id": 123,
  "orderState": "CREATED"
}
```

### 9.2 Crear Orden En PayPal

Endpoint: `POST /api/payments/paypal/create`

Request:

```json
{
  "orderId": 123
}
```

Response:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=8XD12345AB678901C",
  "status": "CREATED"
}
```

### 9.3 Capturar Pago

Endpoint: `POST /api/payments/paypal/capture`

Request:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C"
}
```

Response:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "captureId": "2GG279541U471931P",
  "status": "COMPLETED"
}
```

## 10. Flujo Operativo De Prueba (Paso A Paso)

### Paso 1: Compilar y ejecutar backend

```powershell
mvn -DskipTests compile
```

Luego ejecutar la app desde VS Code o Maven.

### Paso 2: Iniciar ngrok

```powershell
ngrok http 8080
```

### Paso 3: Confirmar webhook URL en PayPal

1. Ir a app sandbox.
2. Revisar Webhook URL.
3. Debe apuntar al dominio ngrok actual + `/api/payments/paypal/webhook`.

### Paso 4: Crear orden local

Llamar `POST /api/orders` con detalles validos.

### Paso 5: Crear orden de pago PayPal

Llamar `POST /api/payments/paypal/create` con `orderId`.

Verificar que la respuesta tenga:

1. `paypalOrderId`
2. `approvalUrl`

### Paso 6: Aprobar en PayPal

Abrir `approvalUrl` en navegador y aprobar pago con cuenta sandbox buyer.

### Paso 7: Capturar pago

Al retornar a front, usar `token` (PayPal order id) y llamar:

`POST /api/payments/paypal/capture`

### Paso 8: Verificar estado en base de datos

Confirmar orden con:

1. `orderState = PAID`
2. `paymentProvider = PAYPAL`
3. `paypalOrderId` informado
4. `paypalCaptureId` informado
5. `paidAt` con fecha/hora

### Paso 9: Verificar webhook

1. Revisar logs backend.
2. Confirmar que webhook llega y se procesa sin error de firma.

## 11. Estados De Orden Y Significado

1. `CREATED`: orden local creada, aun sin iniciar pago.
2. `PENDING_PAYMENT`: pago en proceso/aprobado pendiente de captura.
3. `PAID`: pago capturado correctamente.
4. `PAYMENT_FAILED`: captura rechazada/declinada o evento de falla.
5. `CANCELLED`: cancelada por negocio.
6. `CONFIRMED`: estado legado mantenido por compatibilidad.

## 12. Manejo De Errores Implementado

Codigos estandar de API:

1. `ORDER_NOT_FOUND` -> HTTP 404
2. `PAYMENT_VALIDATION_ERROR` -> HTTP 400
3. `PAYPAL_API_ERROR` -> HTTP 502
4. `WEBHOOK_VERIFICATION_ERROR` -> HTTP 401
5. `INTERNAL_ERROR` -> HTTP 500

Formato:

```json
{
  "code": "PAYMENT_VALIDATION_ERROR",
  "message": "Order id is required",
  "timestamp": "2026-03-13T20:00:00Z",
  "path": "uri=/api/payments/paypal/create"
}
```

## 13. Errores Reales Encontrados Y Solucion

### 13.1 Error DB check constraint de `order_state`

Error:

- Insercion con `CREATED` fallaba por constraint antiguo en tabla `orders`.

Solucion:

- Ejecutar script:
`src/main/resources/sql/fix_orders_order_state_check.sql`

Esto recrea la constraint con estados nuevos.

### 13.2 Error PayPal `DECIMAL_PRECISION`

Error:

- Se enviaban montos con 4 decimales (ej. `4048.9000`).

Solucion aplicada:

- Normalizacion a 2 decimales (`setScale(2, HALF_UP)`) antes de crear orden PayPal.

### 13.3 Error PayPal `CURRENCY_NOT_SUPPORTED`

Error:

- Moneda no habilitada para la cuenta/app.

Solucion aplicada:

- Usar `paypal.currency=USD` en dev.

### 13.4 Error de inyeccion `ObjectMapper`

Error:

- No se encontraba bean `ObjectMapper`.

Solucion aplicada:

- Se registro bean explicito en `BeanConfiguration`.

## 14. Archivos Clave Modificados En Esta Implementacion

1. `pom.xml`
2. `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/PayPalCheckoutService.java`
3. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/paypal/PayPalApiClient.java`
4. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/PayPalPaymentController.java`
5. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/PayPalProperties.java`
6. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/BeanConfiguration.java`
7. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/GlobalExceptionHandler.java`
8. `src/main/java/com/amechato/ecommerce_api/backend/domain/models/Order.java`
9. `src/main/java/com/amechato/ecommerce_api/backend/domain/models/OrderState.java`
10. `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/persistence/entities/OrderEntity.java`
11. `src/main/resources/application-dev.properties`
12. `src/main/resources/application-prd.properties`
13. `src/main/resources/sql/fix_orders_order_state_check.sql`
14. `PAYPAL_INTEGRATION.md`
15. `PAYPAL_FRONTEND_SIGNATURES.md`

## 15. Checklist Final De Cierre

Marca todo antes de presentar:

1. [ ] Backend levanta sin errores.
2. [ ] Ngrok activo y dominio publico vigente.
3. [ ] Webhook URL actualizada en PayPal.
4. [ ] Webhook ID cargado en `application-dev.properties`.
5. [ ] Moneda configurada y soportada (`USD`).
6. [ ] Se crea orden local correctamente.
7. [ ] `create` PayPal devuelve `approvalUrl`.
8. [ ] Usuario aprueba pago en PayPal sandbox.
9. [ ] `capture` devuelve `status=COMPLETED`.
10. [ ] Orden queda `PAID` en base de datos.
11. [ ] Webhook llega y se valida firma.
12. [ ] Se documentaron logs/debug IDs para trazabilidad.

## 16. Guion Corto Para Explicacion Oral

Puedes explicarlo asi:

1. Creamos orden local con estado `CREATED`.
2. El backend crea la orden en PayPal y devuelve `approvalUrl`.
3. El frontend redirige al usuario para aprobar.
4. Al volver, el frontend solicita captura.
5. El backend captura, guarda `paypalCaptureId` y marca `PAID`.
6. Webhook confirma asincronamente para robustez.
7. Implementamos manejo de errores estandar y solucionamos casos reales (constraint DB, precision decimal, moneda, ObjectMapper).
