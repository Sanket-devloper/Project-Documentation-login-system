# Auth Frontend

A reusable authentication frontend (React + TypeScript + Vite + Tailwind),
built to be dropped into any project (real estate, e-commerce, SaaS, admin
portals, etc.) and paired with a Java/Spring Boot backend.

## What's included

- Login, Register, Forgot Password, Reset Password pages
- `AuthContext` + `useAuth()` for app-wide auth state
- `ProtectedRoute` for gating pages like `/dashboard`
- Client-side validation, loading states, error states
- An in-memory **mock auth backend** so the app is fully clickable right now,
  with no Spring Boot API required yet
- A real `apiClient` + `authService` already wired for the planned API
  contract, ready to swap in once the backend exists

## Getting started

```bash
npm install
npm run dev
```

Then open http://localhost:5173. Try the demo account:

```
Email:    demo@example.com
Password: password123
```

Or register a brand-new account — it's stored in memory for the session.

## Switching from mock to the real backend (Phase 6)

1. Set `VITE_API_BASE_URL` in `.env.development` / `.env.production` to your
   Spring Boot API (e.g. `http://localhost:8080/api/v1`).
2. Set `VITE_USE_MOCK_AUTH=false`.
3. `src/services/authService.ts` will now call `apiClient` (real `fetch`
   requests with `credentials: "include"`) instead of `mockBackend`.
4. Delete `src/services/mockBackend.ts` once you no longer need it.

No other file needs to change — components, pages, context, and routes all
go through `authService`, never the backend directly.

## Expected API contract (Spring Boot side)

```
POST   /api/v1/auth/login
POST   /api/v1/auth/register
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
```

## Security notes baked into this frontend

- No passwords or tokens are ever written to `localStorage`/`sessionStorage`
  or logged to the console.
- Auth state is expected to come from a secure, backend-managed session/
  cookie (`HttpOnly`, `Secure`, `SameSite`) — the frontend just asks
  `GET /auth/me` on startup rather than trusting stale client state.
- Login failures show a generic "Invalid email or password" message
  (OWASP guidance, avoids account enumeration).
- `ProtectedRoute` is UX only — it is **not** a security boundary. The
  Spring Boot backend must independently enforce authentication and
  authorization on every request.

## Folder structure

```
src/
├── components/
│   ├── auth/        # AuthLayout, LoginForm, RegisterForm, PasswordInput, ProtectedRoute
│   └── ui/           # Button, Input, Label
├── pages/            # Login, Register, ForgotPassword, ResetPassword, Dashboard
├── context/          # AuthContext
├── hooks/            # useAuth
├── services/         # apiClient, authService, mockBackend
├── types/            # auth.ts
├── routes/           # AppRoutes
└── utils/            # validation.ts
```
