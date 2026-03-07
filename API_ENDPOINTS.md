# Documentacion de Endpoints - Backend Ecommerce API

## Cobertura analizada
Se reviso el backend expuesto en los controllers de `src/main/java/com/amechato/ecommerce_api/backend/infrastructure/controllers` y los modelos/mappers/servicios asociados.

Controllers detectados:
- `CategoryController` (`/api/categories`)
- `ProductController` (`/api/products`)
- `UserController` (`/api/users`)
- `OrderController` (`/api/orders`)

Total de endpoints: **16**

## Base URL
Por configuracion actual (sin `server.port` ni `server.servlet.context-path`), la base por defecto es:

`http://localhost:8080`

Rutas completas = `http://localhost:8080` + path del endpoint.

## Notas globales para integracion React
- No hay configuracion CORS visible (`@CrossOrigin` o `WebMvcConfigurer`) en el backend.
- No hay `@ControllerAdvice` para normalizar errores.
- Varios `findById` retornan `200` con `null` cuando no existe recurso, en lugar de `404`.
- `User` actualmente retorna `password` en responses.
- `Order` puede serializar `totalOrderPrice` por el getter `getTotalOrderPrice()`.

---

## Modelos de datos

### Category
```json
{
  "id": 1,
  "name": "Electronics",
  "createDate": "2026-03-07T10:00:00",
  "updateDate": "2026-03-07T10:00:00"
}
```

### Product
```json
{
  "id": 10,
  "name": "Laptop Pro",
  "code": "LP-001",
  "description": "16GB RAM",
  "urlImage": "https://cdn.example.com/laptop.jpg",
  "price": 1299.99,
  "createDate": "2026-03-07T10:00:00",
  "updateDate": "2026-03-07T10:00:00",
  "userId": 2,
  "categoryId": 1
}
```

### User
```json
{
  "id": 2,
  "username": "jdoe",
  "password": "plain-or-hash",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "555123456",
  "userType": "USER",
  "createDate": "2026-03-07T10:00:00",
  "updateDate": "2026-03-07T10:00:00"
}
```

### OrderDetail
```json
{
  "id": 1,
  "quantity": 2,
  "price": 100.00,
  "productId": 10
}
```

### Order
```json
{
  "id": 100,
  "orderDate": "2026-03-07T12:00:00",
  "orderDetails": [
    { "id": 1, "quantity": 2, "price": 100.00, "productId": 10 }
  ],
  "orderState": "CONFIRMED",
  "userId": 2,
  "totalOrderPrice": 200.00
}
```

Enums:
- `OrderState`: `CANCELLED | CONFIRMED`
- `UserType`: `ADMIN | USER`

---

## CategoryController (`/api/categories`)

### 1. GET `/api/categories`
- Metodo HTTP: `GET`
- Controller: `CategoryController`
- Funcionalidad: listar categorias
- Modelo Request: no aplica
- Modelo Response: `Category[]`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
[
  { "id": 1, "name": "Electronics", "createDate": "2026-03-07T10:00:00", "updateDate": "2026-03-07T10:00:00" }
]
```
- Codigos HTTP posibles: `200`, `500`

### 2. GET `/api/categories/{id}`
- Metodo HTTP: `GET`
- Controller: `CategoryController`
- Funcionalidad: obtener categoria por ID
- Modelo Request: no aplica
- Modelo Response: `Category | null`
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{ "id": 1, "name": "Electronics", "createDate": "2026-03-07T10:00:00", "updateDate": "2026-03-07T10:00:00" }
```
- Codigos HTTP posibles: `200`, `400` (id <= 0), `500`

### 3. POST `/api/categories`
- Metodo HTTP: `POST`
- Controller: `CategoryController`
- Funcionalidad: crear categoria
- Modelo Request: `Category`
- Modelo Response: `Category`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON:
```json
{ "name": "Books" }
```
- Ejemplo Response JSON:
```json
{ "id": 5, "name": "Books", "createDate": "2026-03-07T12:10:00", "updateDate": "2026-03-07T12:10:00" }
```
- Codigos HTTP posibles: `201`, `400`, `500`

### 4. DELETE `/api/categories/{id}`
- Metodo HTTP: `DELETE`
- Controller: `CategoryController`
- Funcionalidad: eliminar categoria por ID
- Modelo Request: no aplica
- Modelo Response: vacio
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{}
```
- Codigos HTTP posibles: `200`, `400` (id <= 0), `500`

---

## ProductController (`/api/products`)

### 5. GET `/api/products`
- Metodo HTTP: `GET`
- Controller: `ProductController`
- Funcionalidad: listar productos
- Modelo Request: no aplica
- Modelo Response: `Product[]`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
[
  {
    "id": 10,
    "name": "Laptop Pro",
    "code": "LP-001",
    "description": "16GB RAM",
    "urlImage": "https://cdn.example.com/laptop.jpg",
    "price": 1299.99,
    "createDate": "2026-03-07T10:00:00",
    "updateDate": "2026-03-07T10:00:00",
    "userId": 2,
    "categoryId": 1
  }
]
```
- Codigos HTTP posibles: `200`, `500`

### 6. GET `/api/products/{id}`
- Metodo HTTP: `GET`
- Controller: `ProductController`
- Funcionalidad: obtener producto por ID
- Modelo Request: no aplica
- Modelo Response: `Product | null`
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{
  "id": 10,
  "name": "Laptop Pro",
  "code": "LP-001",
  "description": "16GB RAM",
  "urlImage": "https://cdn.example.com/laptop.jpg",
  "price": 1299.99,
  "createDate": "2026-03-07T10:00:00",
  "updateDate": "2026-03-07T10:00:00",
  "userId": 2,
  "categoryId": 1
}
```
- Codigos HTTP posibles: `200`, `400` (id <= 0), `500`

### 7. POST `/api/products`
- Metodo HTTP: `POST`
- Controller: `ProductController`
- Funcionalidad: crear producto
- Modelo Request: `Product`
- Modelo Response: `Product`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON:
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
- Ejemplo Response JSON:
```json
{
  "id": 11,
  "name": "Mouse",
  "code": "MSE-001",
  "description": "Wireless",
  "urlImage": "https://cdn.example.com/mouse.jpg",
  "price": 19.99,
  "createDate": "2026-03-07T12:20:00",
  "updateDate": "2026-03-07T12:20:00",
  "userId": 2,
  "categoryId": 1
}
```
- Codigos HTTP posibles: `201`, `400`, `500`

### 8. DELETE `/api/products/{id}`
- Metodo HTTP: `DELETE`
- Controller: `ProductController`
- Funcionalidad: eliminar producto por ID
- Modelo Request: no aplica
- Modelo Response: vacio
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{}
```
- Codigos HTTP posibles: `200`, `400` (id <= 0), `500`

---

## UserController (`/api/users`)

### 9. POST `/api/users`
- Metodo HTTP: `POST`
- Controller: `UserController`
- Funcionalidad: crear usuario
- Modelo Request: `User`
- Modelo Response: `User`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON:
```json
{
  "username": "jdoe",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "555123456",
  "userType": "USER"
}
```
- Ejemplo Response JSON:
```json
{
  "id": 2,
  "username": "jdoe",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "555123456",
  "userType": "USER",
  "createDate": "2026-03-07T12:30:00",
  "updateDate": "2026-03-07T12:30:00"
}
```
- Codigos HTTP posibles: `200`, `400`, `500` (ej. email duplicado)

### 10. GET `/api/users/{id}`
- Metodo HTTP: `GET`
- Controller: `UserController`
- Funcionalidad: obtener usuario por ID
- Modelo Request: no aplica
- Modelo Response: `User | null`
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{
  "id": 2,
  "username": "jdoe",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "555123456",
  "userType": "USER",
  "createDate": "2026-03-07T12:30:00",
  "updateDate": "2026-03-07T12:30:00"
}
```
- Codigos HTTP posibles: `200`, `500`

### 11. GET `/api/users/by-email?email={email}`
- Metodo HTTP: `GET`
- Controller: `UserController`
- Funcionalidad: obtener usuario por email
- Modelo Request: no aplica
- Modelo Response: `User | null`
- Route Params: ninguno
- Query Params:
  - `email: string` (requerido)
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{
  "id": 2,
  "username": "jdoe",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@demo.com",
  "address": "Street 123",
  "cellphone": "555123456",
  "userType": "USER",
  "createDate": "2026-03-07T12:30:00",
  "updateDate": "2026-03-07T12:30:00"
}
```
- Codigos HTTP posibles: `200`, `400` (si falta query param), `500`

---

## OrderController (`/api/orders`)

### 12. GET `/api/orders`
- Metodo HTTP: `GET`
- Controller: `OrderController`
- Funcionalidad: listar ordenes
- Modelo Request: no aplica
- Modelo Response: `Order[]`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
[
  {
    "id": 100,
    "orderDate": "2026-03-07T12:00:00",
    "orderDetails": [
      { "id": 1, "quantity": 2, "price": 100.00, "productId": 10 }
    ],
    "orderState": "CONFIRMED",
    "userId": 2,
    "totalOrderPrice": 200.00
  }
]
```
- Codigos HTTP posibles: `200`, `500`

### 13. GET `/api/orders/{id}`
- Metodo HTTP: `GET`
- Controller: `OrderController`
- Funcionalidad: obtener orden por ID
- Modelo Request: no aplica
- Modelo Response: `Order`
- Route Params:
  - `id: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{
  "id": 100,
  "orderDate": "2026-03-07T12:00:00",
  "orderDetails": [
    { "id": 1, "quantity": 2, "price": 100.00, "productId": 10 }
  ],
  "orderState": "CONFIRMED",
  "userId": 2,
  "totalOrderPrice": 200.00
}
```
- Codigos HTTP posibles: `200`, `500` (si no existe, hoy lanza `RuntimeException`)

### 14. GET `/api/orders/user/{userId}`
- Metodo HTTP: `GET`
- Controller: `OrderController`
- Funcionalidad: listar ordenes por usuario
- Modelo Request: no aplica
- Modelo Response: `Order[]`
- Route Params:
  - `userId: number`
- Query Params: ninguno
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
[
  {
    "id": 100,
    "orderDate": "2026-03-07T12:00:00",
    "orderDetails": [
      { "id": 1, "quantity": 2, "price": 100.00, "productId": 10 }
    ],
    "orderState": "CONFIRMED",
    "userId": 2,
    "totalOrderPrice": 200.00
  }
]
```
- Codigos HTTP posibles: `200`, `500`

### 15. POST `/api/orders`
- Metodo HTTP: `POST`
- Controller: `OrderController`
- Funcionalidad: crear orden y normalizar estado (`CANCELLED` o `CONFIRMED`)
- Modelo Request: `Order`
- Modelo Response: `Order`
- Route Params: ninguno
- Query Params: ninguno
- Ejemplo Request JSON:
```json
{
  "userId": 2,
  "orderState": "CONFIRMED",
  "orderDetails": [
    { "productId": 10, "quantity": 2, "price": 100.00 }
  ]
}
```
- Ejemplo Response JSON:
```json
{
  "id": 101,
  "orderDate": "2026-03-07T13:00:00",
  "orderDetails": [
    { "id": 20, "productId": 10, "quantity": 2, "price": 100.00 }
  ],
  "orderState": "CONFIRMED",
  "userId": 2,
  "totalOrderPrice": 200.00
}
```
- Codigos HTTP posibles: `200`, `400`, `500`
- Nota: si `orderState` llega `null`, puede generar `500` (`NullPointerException`) en el controller actual.

### 16. POST `/api/orders/{id}/state?state={state}`
- Metodo HTTP: `POST`
- Controller: `OrderController`
- Funcionalidad: actualizar estado de una orden
- Modelo Request: no aplica (sin body)
- Modelo Response: vacio
- Route Params:
  - `id: number`
- Query Params:
  - `state: string` (requerido). Si es `CANCELLED` guarda cancelado; cualquier otro valor guarda `CONFIRMED`.
- Ejemplo Request JSON: no aplica
- Ejemplo Response JSON:
```json
{}
```
- Codigos HTTP posibles: `200`, `400` (si falta `state`), `500`

---

## Tipos TypeScript sugeridos para frontend React
```ts
export type UserType = "ADMIN" | "USER";
export type OrderState = "CANCELLED" | "CONFIRMED";

export interface Category {
  id?: number;
  name?: string;
  createDate?: string;
  updateDate?: string;
}

export interface Product {
  id?: number;
  name?: string;
  code?: string;
  description?: string;
  urlImage?: string;
  price?: number;
  createDate?: string;
  updateDate?: string;
  userId?: number;
  categoryId?: number;
}

export interface User {
  id?: number;
  username?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  address?: string;
  cellphone?: string;
  userType?: UserType;
  createDate?: string;
  updateDate?: string;
}

export interface OrderDetail {
  id?: number;
  quantity?: number;
  price?: number;
  productId?: number;
}

export interface Order {
  id?: number;
  orderDate?: string;
  orderDetails?: OrderDetail[];
  orderState?: OrderState;
  userId?: number;
  totalOrderPrice?: number;
}
```
