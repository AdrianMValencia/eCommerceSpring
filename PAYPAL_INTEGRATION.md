# Integracion PayPal - Backend (Spring Boot)

Este documento describe la implementacion del flujo de pagos con PayPal Checkout API en el backend y como integrarlo con el frontend.

## Objetivo Del Flujo

1. El cliente crea una orden en el backend.
2. El backend crea una orden de pago en PayPal.
3. El backend devuelve `approvalUrl` al frontend.
4. El frontend redirige al usuario a PayPal para aprobar el pago.
5. PayPal redirige al frontend (`return-url` / `cancel-url`).
6. El frontend solicita al backend capturar el pago.
7. El backend actualiza el estado de la orden en base de datos.
8. El webhook de PayPal confirma el resultado de forma asincrona.

## Endpoints Implementados

- `POST /api/orders`
- `POST /api/payments/paypal/create`
- `POST /api/payments/paypal/capture`
- `POST /api/payments/paypal/webhook`

## Estados De Orden Usados

Se extendio `OrderState` para soportar el ciclo de pago:

- `CREATED`
- `PENDING_PAYMENT`
- `PAID`
- `PAYMENT_FAILED`
- `CANCELLED`
- `CONFIRMED` (compatibilidad existente)

## Datos De Pago Persistidos En Orden

Ademas de los campos originales, la orden ahora guarda:

- `paymentProvider`
- `paymentStatus`
- `paypalOrderId`
- `paypalCaptureId`
- `paidAt`

## Configuracion

### Desarrollo (`application-dev.properties`)

```properties
paypal.base-url=https://api-m.sandbox.paypal.com
paypal.client-id=${PAYPAL_CLIENT_ID:}
paypal.client-secret=${PAYPAL_CLIENT_SECRET:}
paypal.webhook-id=${PAYPAL_WEBHOOK_ID:}
paypal.return-url=${PAYPAL_RETURN_URL:http://localhost:5173/checkout/paypal/success}
paypal.cancel-url=${PAYPAL_CANCEL_URL:http://localhost:5173/checkout/paypal/cancel}
paypal.currency=${PAYPAL_CURRENCY:USD}
```

### Produccion (`application-prd.properties`)

```properties
paypal.base-url=${PAYPAL_BASE_URL:https://api-m.paypal.com}
paypal.client-id=${PAYPAL_CLIENT_ID:}
paypal.client-secret=${PAYPAL_CLIENT_SECRET:}
paypal.webhook-id=${PAYPAL_WEBHOOK_ID:}
paypal.return-url=${PAYPAL_RETURN_URL:https://app.example.com/checkout/paypal/success}
paypal.cancel-url=${PAYPAL_CANCEL_URL:https://app.example.com/checkout/paypal/cancel}
paypal.currency=${PAYPAL_CURRENCY:USD}
```

## Variables De Entorno Requeridas

- `PAYPAL_CLIENT_ID`
- `PAYPAL_CLIENT_SECRET`
- `PAYPAL_WEBHOOK_ID`
- `PAYPAL_RETURN_URL` (opcional, tiene default)
- `PAYPAL_CANCEL_URL` (opcional, tiene default)
- `PAYPAL_CURRENCY` (opcional, default `USD`)

## Ejemplo Completo De Integracion

### 1) Crear Orden Local

```http
POST /api/orders
Content-Type: application/json
```

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

### 2) Crear Orden En PayPal

```http
POST /api/payments/paypal/create
Content-Type: application/json
```

```json
{
  "orderId": 123
}
```

Respuesta esperada:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=8XD12345AB678901C",
  "status": "CREATED"
}
```

### 3) Redireccionar Al Usuario

El frontend redirige el navegador a `approvalUrl`.

### 4) Capturar Pago

Despues del retorno de PayPal, el frontend llama:

```http
POST /api/payments/paypal/capture
Content-Type: application/json
```

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C"
}
```

Respuesta esperada:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "captureId": "2GG279541U471931P",
  "status": "COMPLETED"
}
```

Si el estado es `COMPLETED`, la orden pasa a `PAID`.

## Webhooks De PayPal

Endpoint backend:

- `POST /api/payments/paypal/webhook`

El backend:

1. Valida firma del webhook con PayPal (`/v1/notifications/verify-webhook-signature`).
2. Procesa eventos relevantes:
- `CHECKOUT.ORDER.APPROVED`
- `PAYMENT.CAPTURE.COMPLETED`
- `PAYMENT.CAPTURE.DENIED`
- `PAYMENT.CAPTURE.DECLINED`
- `PAYMENT.CAPTURE.REFUNDED`
3. Actualiza estado local de la orden.

## Manejo De Errores

Se implemento `@RestControllerAdvice` con respuestas estandar:

- `ORDER_NOT_FOUND` -> `404`
- `PAYMENT_VALIDATION_ERROR` -> `400`
- `PAYPAL_API_ERROR` -> `502`
- `WEBHOOK_VERIFICATION_ERROR` -> `401`
- `INTERNAL_ERROR` -> `500`

Formato de error:

```json
{
  "code": "PAYMENT_VALIDATION_ERROR",
  "message": "Order id is required",
  "timestamp": "2026-03-13T20:00:00Z",
  "path": "uri=/api/payments/paypal/create"
}
```

## Integracion En Local (Ngrok)

PayPal no puede llamar a `localhost` directamente para webhooks. En desarrollo usa tunel HTTPS.

1. Ejecuta backend en `8080`.
2. Ejecuta:

```powershell
ngrok http 8080
```

3. Registra en PayPal la URL:

`https://<tu-dominio-ngrok>/api/payments/paypal/webhook`

## Buenas Practicas Para Produccion

1. Nunca exponer `client-secret` en frontend ni en repositorio.
2. Usar HTTPS para `return-url`, `cancel-url` y webhook.
3. Validar firma del webhook antes de persistir cambios.
4. Implementar idempotencia para capturas y eventos webhook repetidos.
5. Registrar correlacion de IDs: `orderId`, `paypalOrderId`, `captureId`.
6. Monitorear pagos fallidos y reintentos segun reglas de negocio.
7. Validar montos (backend vs PayPal) antes de marcar `PAID`.
8. Separar credenciales Sandbox y Produccion por entorno.

## Archivos Clave De La Implementacion

- `src/main/java/com/amechato/ecommerce_api/backend/application/usecases/PayPalCheckoutService.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/paypal/PayPalApiClient.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/PayPalPaymentController.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/payment/CreatePaypalPaymentRequest.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/payment/CreatePaypalPaymentResponse.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/payment/CapturePaypalPaymentRequest.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers/dto/payment/CapturePaypalPaymentResponse.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/errors/GlobalExceptionHandler.java`
- `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/config/PayPalProperties.java`
