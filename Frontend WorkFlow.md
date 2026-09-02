# Auth Frontend — Project Documentation

A reusable authentication frontend built with **React + TypeScript + Vite + Tailwind CSS**, designed to plug into any project and pair with a Spring Boot backend later. This document explains every file, what it does, and how data flows from one page/component to the next.

---

## 1. Tech Stack

| Layer | Choice |
|---|---|
| Framework | React 18 + TypeScript |
| Build tool | Vite |
| Styling | Tailwind CSS |
| Routing | react-router-dom v6 |
| Icons | lucide-react |
| State | React Context (`AuthContext`) — no Redux needed for this scope |
| Backend (current) | In-memory mock (`mockBackend.ts`) |
| Backend (future) | Spring Boot REST API via `apiClient.ts` |

---

## 2. Folder Structure

```
auth-frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── postcss.config.js
├── .env.development
├── .env.example
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── index.css
    ├── vite-env.d.ts
    │
    ├── components/
    │   ├── auth/
    │   │   ├── AuthLayout.tsx
    │   │   ├── LoginForm.tsx
    │   │   ├── RegisterForm.tsx
    │   │   ├── PasswordInput.tsx
    │   │   └── ProtectedRoute.tsx
    │   └── ui/
    │       ├── Button.tsx
    │       ├── Input.tsx
    │       └── Label.tsx
    │
    ├── pages/
    │   ├── Login.tsx
    │   ├── Register.tsx
    │   ├── ForgotPassword.tsx
    │   ├── ResetPassword.tsx
    │   └── Dashboard.tsx
    │
    ├── context/
    │   └── AuthContext.tsx
    │
    ├── hooks/
    │   └── useAuth.ts
    │
    ├── services/
    │   ├── apiClient.ts
    │   ├── authService.ts
    │   └── mockBackend.ts
    │
    ├── routes/
    │   └── AppRoutes.tsx
    │
    ├── types/
    │   └── auth.ts
    │
    └── utils/
        └── validation.ts
```

---

## 3. What Each File Does

### Root / config

| File | Purpose |
|---|---|
| `index.html` | HTML shell; loads `src/main.tsx` as a module |
| `main.tsx` | Mounts `<App />` into `#root` |
| `App.tsx` | Wraps the whole app in `<BrowserRouter>` and `<AuthProvider>`, renders `<AppRoutes />` |
| `index.css` | Tailwind base/components/utilities + a couple of global resets |
| `vite-env.d.ts` | TypeScript types for `import.meta.env` variables |
| `.env.development` | Sets `VITE_API_BASE_URL` and `VITE_USE_MOCK_AUTH=true` |

### `types/auth.ts`
Single source of truth for shared shapes: `User`, `LoginRequest`, `RegisterRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`, `AuthResult`, and the `ApiError` class. Both the mock backend and the future real backend produce data that fits these types.

### `utils/validation.ts`
Pure, framework-free functions: `validateEmail`, `validateLoginPassword`, `validateRegisterPassword`, `validateName`, `validateConfirmPassword`. No side effects — just take a string, return an error message or `undefined`.

### `services/apiClient.ts`
The real HTTP client for when Spring Boot exists. Wraps `fetch`, always sends `credentials: "include"` (so an HttpOnly session cookie is sent automatically), and converts non-2xx responses into a typed `ApiError` with a user-friendly message.

### `services/mockBackend.ts`
An in-memory fake server. Holds a `users` array and a `currentSessionUserId` in module scope (not localStorage). Simulates network delay with `setTimeout`. Comes seeded with a demo account (`demo@example.com` / `password123`). **Delete this file once the real backend is live.**

### `services/authService.ts`
The **only** file the rest of the app talks to for auth actions. Reads `VITE_USE_MOCK_AUTH` and routes each call (`login`, `register`, `logout`, `getCurrentUser`, `forgotPassword`, `resetPassword`) to either `mockBackend` or `apiClient`. This indirection is what lets Phase 6 (real backend) require changing exactly one file.

### `context/AuthContext.tsx`
React Context holding `{ user, isAuthenticated, loading }` plus the `login/register/logout` functions. On mount, it calls `authService.getCurrentUser()` to verify the session with the backend — it never assumes a user is logged in just because of old UI state.

### `hooks/useAuth.ts`
Thin `useContext(AuthContext)` wrapper that throws a clear error if used outside `<AuthProvider>`. This is the hook every component actually imports.

### `components/ui/*`
Generic, auth-agnostic building blocks — `Button`, `Input`, `Label`. They know nothing about authentication; they could be reused in any other part of the app.

### `components/auth/*`
| File | Purpose |
|---|---|
| `AuthLayout.tsx` | Shared visual shell (logo, title, card, footer) for every auth screen |
| `PasswordInput.tsx` | `Input` + a show/hide toggle |
| `LoginForm.tsx` | Owns email/password state, validates, calls `useAuth().login()`, redirects on success |
| `RegisterForm.tsx` | Same pattern for name/email/password/confirm-password, calls `useAuth().register()` |
| `ProtectedRoute.tsx` | Reads `isAuthenticated`/`loading` from `useAuth()`; redirects to `/login` if not authenticated, otherwise renders its children |

### `pages/*`
Thin composition layers — each page just arranges `AuthLayout` + the relevant form/content. They hold almost no logic themselves.

| Page | Route | Contains |
|---|---|---|
| `Login.tsx` | `/login` | `AuthLayout` + `LoginForm` |
| `Register.tsx` | `/register` | `AuthLayout` + `RegisterForm` |
| `ForgotPassword.tsx` | `/forgot-password` | Its own small form calling `authService.forgotPassword()` directly |
| `ResetPassword.tsx` | `/reset-password?token=...` | Reads `token` from the URL query string, calls `authService.resetPassword()` |
| `Dashboard.tsx` | `/dashboard` (protected) | Displays `user` from `useAuth()`, has a logout button |

### `routes/AppRoutes.tsx`
Maps every URL to a page. `/` redirects to `/login`. `/dashboard` is wrapped in `<ProtectedRoute>`. Any unknown path falls back to `/login`.

---

## 4. Page-to-Page Flow

### 4.1 First load (`/`)
```
main.tsx → App.tsx
  → BrowserRouter
  → AuthProvider (starts loading = true, calls authService.getCurrentUser())
  → AppRoutes
      "/" → <Navigate to="/login" />
```
While `AuthContext` is still checking the session, `ProtectedRoute` (if you'd landed on `/dashboard`) shows "Checking your session…" instead of flashing content.

### 4.2 Login flow (`/login`)
```
User opens /login
  → Login.tsx renders AuthLayout + LoginForm
  → User types email/password, clicks "Login"
  → LoginForm validates via utils/validation.ts
      invalid → show inline field errors, stop
      valid   → setLoading(true)
  → calls useAuth().login({ email, password })
  → AuthContext.login() calls authService.login()
  → authService routes to mockBackend.login() (or apiClient.post("/auth/login") later)
      failure → throws ApiError → LoginForm shows "Invalid email or password"
      success → returns { user }
  → AuthContext calls setUser(user) → isAuthenticated becomes true app-wide
  → LoginForm calls navigate("/dashboard")
  → AppRoutes renders <ProtectedRoute><Dashboard /></ProtectedRoute>
  → ProtectedRoute sees isAuthenticated = true → renders Dashboard
```

### 4.3 Register flow (`/register`)
Same shape as login, using `RegisterForm.tsx`:
```
/register → RegisterForm validates name/email/password/confirmPassword
  → useAuth().register(...) → authService.register(...) → mock/real backend
  → new user created AND logged in immediately
  → navigate("/dashboard")
```

### 4.4 Forgot / Reset password flow
```
/login → "Forgot password?" link → /forgot-password
  → user enters email → authService.forgotPassword(email)
  → always shows the same success message (doesn't reveal if the email exists)

(later, from an email link) → /reset-password?token=xyz
  → ResetPassword.tsx reads ?token= from the URL
  → user sets a new password → authService.resetPassword({ token, newPassword })
  → success → navigate("/login")
```

### 4.5 Visiting a protected page directly (`/dashboard`)
```
User navigates to /dashboard without being logged in
  → AppRoutes wraps it in <ProtectedRoute>
  → ProtectedRoute checks useAuth(): loading? → show spinner text
                                     not authenticated? → <Navigate to="/login" state={{ from: location }} />
  → LoginForm reads location.state.from after a successful login
  → redirects the user back to /dashboard instead of a hardcoded default
```

### 4.6 Logout flow
```
Dashboard → "Logout" button → useAuth().logout()
  → AuthContext.logout() calls authService.logout()
  → mockBackend clears currentSessionUserId (or real backend clears the cookie session)
  → AuthContext calls setUser(null) → isAuthenticated becomes false
  → Next time a ProtectedRoute check runs, it redirects to /login
```

---

## 5. Key Design Decisions

- **One-way data flow for auth state**: only `AuthContext` calls `setUser`. Every component reads state via `useAuth()` and never manages its own copy of "am I logged in."
- **Services isolate the network**: components never call `fetch` or import `mockBackend`/`apiClient` directly — only `authService.ts` does. This is what makes the Phase 6 backend swap a one-file change.
- **No tokens in browser storage**: `mockBackend` keeps session state in module memory (not `localStorage`/`sessionStorage`), mirroring how a real HttpOnly cookie session behaves — nothing sensitive is ever written to the browser's storage APIs.
- **`ProtectedRoute` is UX, not security**: it just prevents rendering a page client-side. The Spring Boot backend must independently authorize every API request — the frontend gate is never the real security boundary.
- **Generic error messages**: failed logins always say "Invalid email or password" rather than "user not found" / "wrong password," to avoid leaking which part was wrong (OWASP guidance against account enumeration).

---

## 6. Switching from Mock to Real Backend (Phase 6)

1. In `.env.development` (or `.env.production`), set:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   VITE_USE_MOCK_AUTH=false
   ```
2. `authService.ts` will now call `apiClient` instead of `mockBackend` for every function — no other file changes.
3. Delete `src/services/mockBackend.ts` once it's no longer needed.

Expected Spring Boot endpoints:
```
POST   /api/v1/auth/login
POST   /api/v1/auth/register
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
```
