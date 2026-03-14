# Firmas De Integracion PayPal (Backend + React)

Este documento define las firmas (contratos) para integrar pagos con PayPal desde tu frontend en React.

## Base URL Backend

- Desarrollo: `http://localhost:8080`
- Prefijo API: `/api`

## Endpoints Backend

### 1) Crear Orden Local

- Metodo: `POST`
- Ruta: `/api/orders`
- Body (ejemplo minimo):

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

- Response (resumen):

```json
{
  "id": 123,
  "orderState": "CREATED"
}
```

### 2) Crear Orden De Pago En PayPal

- Metodo: `POST`
- Ruta: `/api/payments/paypal/create`
- Body:

```json
{
  "orderId": 123
}
```

- Response:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=8XD12345AB678901C",
  "status": "CREATED"
}
```

### 3) Capturar Pago En PayPal

- Metodo: `POST`
- Ruta: `/api/payments/paypal/capture`
- Body:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C"
}
```

- Response:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "captureId": "2GG279541U471931P",
  "status": "COMPLETED"
}
```

### 4) Webhook (No Lo Llama React)

- Metodo: `POST`
- Ruta: `/api/payments/paypal/webhook`
- Consumidor: PayPal (server to server)

## Firmas TypeScript (Frontend)

```ts
export type OrderState =
  | 'CREATED'
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'PAYMENT_FAILED'
  | 'CANCELLED'
  | 'CONFIRMED';

export interface OrderDetailRequest {
  productId: number;
  quantity: number;
  price: number;
}

export interface CreateOrderRequest {
  userId: number;
  orderDetails: OrderDetailRequest[];
}

export interface OrderResponse {
  id: number;
  orderDate?: string;
  orderState: OrderState;
  userId: number;
  paymentProvider?: string;
  paymentStatus?: string;
  paypalOrderId?: string;
  paypalCaptureId?: string;
  paidAt?: string;
}

export interface CreatePaypalPaymentRequest {
  orderId: number;
}

export interface CreatePaypalPaymentResponse {
  orderId: number;
  paypalOrderId: string;
  approvalUrl: string;
  status: string;
}

export interface CapturePaypalPaymentRequest {
  orderId: number;
  paypalOrderId: string;
}

export interface CapturePaypalPaymentResponse {
  orderId: number;
  paypalOrderId: string;
  captureId: string;
  status: string;
}

export interface ApiErrorResponse {
  code:
    | 'ORDER_NOT_FOUND'
    | 'PAYMENT_VALIDATION_ERROR'
    | 'PAYPAL_API_ERROR'
    | 'WEBHOOK_VERIFICATION_ERROR'
    | 'INTERNAL_ERROR';
  message: string;
  timestamp: string;
  path: string;
}
```

## Firmas De Cliente HTTP (Frontend)

```ts
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export const createOrder = async (payload: CreateOrderRequest) => {
  const { data } = await api.post<OrderResponse>('/orders', payload);
  return data;
};

export const createPaypalPayment = async (orderId: number) => {
  const { data } = await api.post<CreatePaypalPaymentResponse>('/payments/paypal/create', { orderId });
  return data;
};

export const capturePaypalPayment = async (payload: CapturePaypalPaymentRequest) => {
  const { data } = await api.post<CapturePaypalPaymentResponse>('/payments/paypal/capture', payload);
  return data;
};
```

## Firmas De Rutas React

Define al menos estas rutas en React Router:

- `/checkout/paypal/success`
- `/checkout/paypal/cancel`

## Flujo React Recomendado

1. Crear orden local (`POST /orders`).
2. Crear orden PayPal (`POST /payments/paypal/create`).
3. Redirigir a `approvalUrl`.
4. PayPal retorna a `success` o `cancel`.
5. En `success`, leer query params (`token`, `PayerID`) y llamar `capture`.
6. Mostrar resultado al usuario y refrescar estado de la orden.

## Ejemplo De Pagina Success (Firma)

```ts
// token normalmente corresponde al paypalOrderId
interface PaypalSuccessQuery {
  token?: string;
  PayerID?: string;
}
```

Captura sugerida:

```ts
const captureFromSuccess = async (orderId: number, token: string) => {
  return await capturePaypalPayment({
    orderId,
    paypalOrderId: token
  });
};
```

## Estados HTTP Esperados

- `200`: operaciones exitosas (`create`, `capture`).
- `204`: webhook procesado.
- `400`: validacion de request.
- `401`: firma de webhook invalida.
- `404`: orden no encontrada.
- `502`: error al consumir PayPal API.
- `500`: error interno no controlado.

## Recomendaciones De Integracion

1. En frontend, guardar `orderId` localmente antes de redirigir a PayPal.
2. Tratar `capture` como operacion idempotente (si ya esta pagada, backend responde estado actual).
3. No confiar solo en el redirect; el estado final tambien puede llegar por webhook.
4. Mostrar mensajes amigables segun `ApiErrorResponse.code`.

## Prompt Profesional Para El Frontend

Usa este prompt para tu equipo frontend o para un asistente AI que implemente el flujo:

```text
Actua como Senior Frontend Engineer especializado en React + TypeScript + React Router + Axios.

Objetivo:
Implementar el flujo de checkout con PayPal consumiendo mi backend Spring Boot.

Contexto tecnico:
- Backend base URL: http://localhost:8080/api
- Endpoints:
  1) POST /orders
  2) POST /payments/paypal/create
  3) POST /payments/paypal/capture
- El webhook lo maneja solo backend (no frontend).

Contratos API (obligatorio respetar):
- CreatePaypalPaymentRequest: { orderId: number }
- CreatePaypalPaymentResponse: { orderId: number, paypalOrderId: string, approvalUrl: string, status: string }
- CapturePaypalPaymentRequest: { orderId: number, paypalOrderId: string }
- CapturePaypalPaymentResponse: { orderId: number, paypalOrderId: string, captureId: string, status: string }

Requisitos funcionales:
1) Crear orden local antes de iniciar pago.
2) Llamar create payment y redirigir a approvalUrl.
3) Implementar ruta /checkout/paypal/success:
   - Leer query params token y PayerID.
   - Recuperar orderId guardado previamente.
   - Ejecutar capture usando { orderId, paypalOrderId: token }.
4) Implementar ruta /checkout/paypal/cancel con mensaje de pago cancelado.
5) Mostrar estados de UI: loading, success, error.
6) Manejar errores con formato ApiErrorResponse del backend.

Requisitos tecnicos:
- Crear archivos separados:
  - src/services/paypal.api.ts
  - src/types/paypal.types.ts
  - src/pages/checkout/PaypalSuccessPage.tsx
  - src/pages/checkout/PaypalCancelPage.tsx
  - src/hooks/usePaypalCheckout.ts (opcional)
- Usar TypeScript estricto.
- No usar any.
- Manejo robusto de null/undefined.
- Implementar mensajes de error claros para usuario.

Entregables esperados:
1) Codigo completo por archivo.
2) Ejemplo de integracion en un boton "Pagar con PayPal".
3) Configuracion de rutas React Router.
4) Breve guia de prueba manual del flujo end-to-end.

Criterios de aceptacion:
- Al aprobar en PayPal, el frontend captura correctamente y muestra confirmacion.
- Al cancelar, el frontend muestra estado de cancelacion sin romper el checkout.
- Los errores de backend se renderizan de forma controlada.
```
