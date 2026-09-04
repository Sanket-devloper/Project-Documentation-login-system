# Auth Backend — React Frontend Aligned

Ready-to-run Java 17 + Spring Boot authentication backend for the supplied React/Vite authentication frontend.

## API contract

| Frontend action | Backend endpoint | Response |
|---|---|---|
| Login | `POST /api/v1/auth/login` | `{ "user": {...} }` + HttpOnly `JSESSIONID` cookie |
| Register | `POST /api/v1/auth/register` | Creates account, logs in immediately, returns `{ "user": {...} }` |
| Current user | `GET /api/v1/auth/me` | Current user object |
| Logout | `POST /api/v1/auth/logout` | Invalidates session |
| Forgot password | `POST /api/v1/auth/forgot-password` | Generic message |
| Reset password | `POST /api/v1/auth/reset-password` | Accepts `{ token, newPassword }` |

## 1. Start PostgreSQL

```bash
docker compose up -d
```

Default database:

- database: `authdb`
- username: `authuser`
- password: `authpass`
- port: `5432`

## 2. Run backend

```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

## 3. Connect your existing frontend

Set your React frontend `.env.development` to:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCK_AUTH=false
```

Your existing `apiClient.ts` must keep `credentials: "include"`. No browser token storage is needed.

## 4. Important request/response shapes

### Register

```json
POST /api/v1/auth/register
{
  "name": "Sanket",
  "email": "user@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "user": {
    "id": "1",
    "name": "Sanket",
    "email": "user@example.com",
    "role": "USER",
    "roles": ["USER"]
  }
}
```

Registration establishes the login session immediately, matching the supplied frontend workflow.

### Login

```json
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Current user

```http
GET /api/v1/auth/me
```

Returns the user directly (not wrapped in `user`).

### Forgot password

```json
POST /api/v1/auth/forgot-password
{
  "email": "user@example.com"
}
```

The response is deliberately identical whether or not the email exists.

For local development, the backend writes the reset URL to its application log. Example:

```text
Password reset link for user@example.com: http://localhost:5173/reset-password?token=...
```

Plug a mail provider into `PasswordResetService` for production delivery.

### Reset password

```json
POST /api/v1/auth/reset-password
{
  "token": "...",
  "newPassword": "newPassword123"
}
```

## 5. Run tests

```bash
mvn clean test
```

## Project structure

```text
src/main/java/com/example/auth/
├── auth/
│   ├── controller/AuthController.java
│   ├── dto/
│   ├── mapper/AuthMapper.java
│   └── service/AuthService.java
├── config/
│   ├── DataInitializer.java
│   └── SecurityConfig.java
├── exception/
├── password/
├── role/
├── security/
└── user/
```

## Security notes

- Passwords are BCrypt hashed and never returned.
- Authentication is server-session based; browser receives an HttpOnly `JSESSIONID` cookie.
- CORS is restricted to `FRONTEND_URL` and credentials are allowed.
- `/login`, `/register`, `/forgot-password`, `/reset-password` are public.
- `/me`, `/logout`, and all unspecified endpoints require authentication.
- `/api/v1/admin/**` requires `ROLE_ADMIN`.
- Reset tokens are random and only a SHA-256 hash is stored in the database.
- CSRF is disabled because the supplied frontend does not currently send a CSRF token. Before production deployment, add a CSRF-token contract to the frontend/backend or use another deliberate CSRF defense appropriate to your deployment topology.
- Set `SESSION_COOKIE_SECURE=true` behind HTTPS in production.

## Error format

```json
{
  "timestamp": "2026-09-02T12:00:00Z",
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid email or password.",
  "errors": {}
}
```
