# Firmas De Endpoints Para Frontend (React)

Documento de contratos API para consumo desde React, basado en la implementacion actual del backend.

## Base URL

- `http://localhost:8080/api`

## Autenticacion

- Tipo: JWT Bearer Token
- Header para endpoints protegidos:

```http
Authorization: Bearer <token>
```

## Matriz De Acceso Por Rol

1. Publico:
- `POST /auth/login`
- `POST /users` (registro)

2. Solo `ADMIN`:
- Todo `OrderController` (`/orders/**`)
- Todo `PayPalPaymentController` (`/payments/paypal/**`)

3. `USER` o `ADMIN` autenticados:
- Resto de endpoints (`/categories/**`, `/products/**`, `GET /users/**`)

## Endpoints De Auth

### POST `/auth/login`

- Auth: Publico
- Request:

```json
{
  "email": "john@demo.com",
  "password": "123456"
}
```

- Response 200:

```json
{
  "token": "<JWT>",
  "userId": 1,
  "email": "john@demo.com",
  "userType": "USER"
}
```

- Response 401:

```json
{
  "code": "AUTH_ERROR",
  "message": "Invalid credentials",
  "timestamp": "2026-03-14T12:00:00Z",
  "path": "uri=/api/auth/login"
}
```

## Endpoints De User

### POST `/users`

- Auth: Publico
- Request:

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

- Response 200 (password no se devuelve):

```json
{
  "id": 1,
  "username": "jdoe",
  "password": null,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "999999999",
  "userType": "USER",
  "createDate": "2026-03-14T10:00:00",
  "updateDate": "2026-03-14T10:00:00"
}
```

### GET `/users/{id}`

- Auth: `USER` o `ADMIN`
- Response 200: mismo modelo de usuario (con `password: null`)

### GET `/users/by-email?email={email}`

- Auth: `USER` o `ADMIN`
- Response 200: mismo modelo de usuario (con `password: null`)

## Endpoints De Categories

### GET `/categories`

- Auth: `USER` o `ADMIN`
- Response 200:

```json
[
  {
    "id": 1,
    "name": "Electronics",
    "createDate": "2026-03-14T10:00:00",
    "updateDate": "2026-03-14T10:00:00"
  }
]
```

### GET `/categories/{id}`

- Auth: `USER` o `ADMIN`
- Response 200: `Category`
- Response 400: id invalido

### POST `/categories`

- Auth: `USER` o `ADMIN`
- Request:

```json
{
  "name": "Books"
}
```

- Response 201: `Category`

### DELETE `/categories/{id}`

- Auth: `USER` o `ADMIN`
- Response 200 sin body
- Response 400: id invalido

## Endpoints De Products

### GET `/products`

- Auth: `USER` o `ADMIN`
- Response 200: `Product[]`

### GET `/products/{id}`

- Auth: `USER` o `ADMIN`
- Response 200: `Product`
- Response 400: id invalido

### POST `/products`

- Auth: `USER` o `ADMIN`
- Request ejemplo:

```json
{
  "name": "Mouse",
  "code": "MSE-001",
  "description": "Wireless",
  "urlImage": "https://cdn.example.com/mouse.jpg",
  "price": 19.99,
  "userId": 2,
  "categoryId": 1
}
```

- Response 201: `Product`

### DELETE `/products/{id}`

- Auth: `USER` o `ADMIN`
- Response 200 sin body
- Response 400: id invalido

## Endpoints De Orders (Solo ADMIN)

### GET `/orders`

- Auth: `ADMIN`
- Response 200: `Order[]`

### GET `/orders/{id}`

- Auth: `ADMIN`
- Response 200: `Order`

### GET `/orders/user/{userId}`

- Auth: `ADMIN`
- Response 200: `Order[]`

### POST `/orders`

- Auth: `ADMIN`
- Request ejemplo:

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

- Response 200: `Order` (estado inicial `CREATED`)

### POST `/orders/{id}/state?state={STATE}`

- Auth: `ADMIN`
- Response 200 sin body

## Endpoints De PayPal (Solo ADMIN)

### POST `/payments/paypal/create`

- Auth: `ADMIN`
- Request:

```json
{
  "orderId": 123
}
```

- Response 200:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=8XD12345AB678901C",
  "status": "CREATED"
}
```

### POST `/payments/paypal/capture`

- Auth: `ADMIN`
- Request:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C"
}
```

- Response 200:

```json
{
  "orderId": 123,
  "paypalOrderId": "8XD12345AB678901C",
  "captureId": "2GG279541U471931P",
  "status": "COMPLETED"
}
```

### POST `/payments/paypal/webhook`

- Auth: `ADMIN` (segun configuracion actual)
- Consumidor esperado: PayPal
- Response 204 sin body

## Firmas TypeScript Recomendadas

```ts
export type UserType = 'USER' | 'ADMIN';

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  userId: number;
  email: string;
  userType: UserType;
};

export type ApiErrorResponse = {
  code: string;
  message: string;
  timestamp: string;
  path: string;
};
```

## Ejemplo De Consumo En React (axios)

```ts
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export const login = async (email: string, password: string) => {
  const { data } = await api.post<LoginResponse>('/auth/login', { email, password });
  localStorage.setItem('token', data.token);
  localStorage.setItem('userType', data.userType);
  return data;
};

export const withAuth = () => {
  const token = localStorage.getItem('token');
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  };
};

export const getProducts = async () => {
  const { data } = await api.get('/products', withAuth());
  return data;
};
```

## Notas Para Frontend

1. Si el usuario es `USER`, no debe ver UI de administracion de ordenes/pagos.
2. Si llama endpoint ADMIN con token USER, backend respondera `403`.
3. Si no envia token en endpoint protegido, backend respondera `401`.
