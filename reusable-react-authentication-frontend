# Reusable React Authentication Frontend --- Development Documentation

## 1. Purpose

This document is the single development guide for building a reusable,
professional authentication frontend in React.

The goal is to create an authentication UI that can be reused across
projects such as:

-   Real Estate
-   E-commerce
-   Admin portals
-   SaaS applications
-   Employee management systems
-   Healthcare applications
-   Other React + Java/Spring Boot applications

The frontend is responsible for:

``` text
UI
↓
Form handling
↓
Client-side validation
↓
API communication
↓
Authentication state
↓
Navigation
↓
Protected frontend routes
```

The Java/Spring Boot backend is responsible for:

``` text
User verification
↓
Password verification
↓
Authentication/session mechanism
↓
Authorization
↓
Database operations
↓
Security enforcement
```

Authentication and authorization must not be treated as the same thing.
Authentication verifies who the user is; authorization determines what
that authenticated user is allowed to access.

------------------------------------------------------------------------

# 2. Technology Stack

## Frontend

Use:

-   React
-   TypeScript
-   Vite
-   React Router
-   Tailwind CSS
-   Shadcn UI
-   Fetch API or Axios
-   ESLint
-   Prettier

## Backend

The frontend will communicate with:

-   Java
-   Spring Boot
-   Spring Security
-   REST APIs
-   PostgreSQL/MySQL or another relational database

The backend technology is intentionally separated from the reusable
React authentication UI.

------------------------------------------------------------------------

# 3. Core Architecture

The complete application will follow:

``` text
                    Browser
                       |
                       ↓
              React Frontend
                       |
              HTTP / JSON / HTTPS
                       |
                       ↓
              Spring Boot API
                       |
                Spring Security
                       |
                    Service
                       |
                  Repository
                       |
                       ↓
                   Database
```

The React application should never directly connect to the database.

React communicates with Java through APIs.

Example:

``` text
POST /api/v1/auth/login
```

Request:

``` json
{
  "email": "user@example.com",
  "password": "user-password"
}
```

The Spring Boot backend validates the credentials and establishes the
authenticated session according to the application's security design.

------------------------------------------------------------------------

# 4. Authentication vs Authorization

## Authentication

Authentication answers:

> Who are you?

Example:

``` text
Email + Password
        ↓
Backend verifies identity
        ↓
Authenticated user
```

## Authorization

Authorization answers:

> What are you allowed to do?

Example:

``` text
Authenticated User
        |
        ├── USER → View properties
        |
        └── ADMIN → View + Add + Update + Delete properties
```

The backend must enforce authorization. Frontend route guards are for
user experience and navigation; they are not a replacement for backend
authorization.

------------------------------------------------------------------------

# 5. Project Goals

The reusable authentication frontend should provide:

-   Login
-   Registration
-   Logout
-   Forgot password
-   Reset password
-   Password visibility toggle
-   Client-side validation
-   Loading states
-   Error states
-   Success feedback
-   Authentication state
-   Protected routes
-   Redirect after login
-   Redirect after logout
-   API service abstraction
-   Reusable UI components
-   Responsive design
-   Accessibility basics
-   Clean folder structure
-   Backend-independent UI components

------------------------------------------------------------------------

# 6. Recommended Folder Structure

``` text
src/
│
├── components/
│   ├── auth/
│   │   ├── AuthLayout.tsx
│   │   ├── LoginForm.tsx
│   │   ├── RegisterForm.tsx
│   │   ├── PasswordInput.tsx
│   │   └── ProtectedRoute.tsx
│   │
│   └── ui/
│       ├── Button.tsx
│       ├── Input.tsx
│       ├── Label.tsx
│       └── ...
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
│   └── authService.ts
│
├── types/
│   └── auth.ts
│
├── routes/
│   └── AppRoutes.tsx
│
├── utils/
│   └── validation.ts
│
├── App.tsx
├── main.tsx
└── index.css
```

------------------------------------------------------------------------

# 7. Responsibility of Each Folder

## components/

Reusable UI pieces.

Examples:

``` text
LoginForm
PasswordInput
AuthLayout
Button
Input
```

Components should focus primarily on presentation and interaction.

------------------------------------------------------------------------

## pages/

Page-level components.

Examples:

``` text
/login
/register
/forgot-password
/reset-password
/dashboard
```

A page composes smaller components.

Example:

``` tsx
function Login() {
  return (
    <AuthLayout>
      <LoginForm />
    </AuthLayout>
  );
}
```

------------------------------------------------------------------------

## context/

Stores application-wide authentication state.

Example:

``` text
currentUser
isAuthenticated
loading
login()
logout()
```

------------------------------------------------------------------------

## hooks/

Reusable React logic.

Example:

``` tsx
const { user, isAuthenticated, login, logout } = useAuth();
```

------------------------------------------------------------------------

## services/

Communication with the Java backend.

Example:

``` text
authService.ts
apiClient.ts
```

Components should not contain repeated API implementation.

------------------------------------------------------------------------

## types/

Shared TypeScript types.

Example:

``` ts
export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
  role: string;
}
```

------------------------------------------------------------------------

## utils/

Generic helper functions.

Examples:

``` text
email validation
password validation
formatting
```

------------------------------------------------------------------------

# 8. Login Page Design

The standard login page should contain:

``` text
Logo
↓
Welcome message
↓
Email input
↓
Password input
↓
Remember me (only if supported by backend/session design)
↓
Forgot password
↓
Login button
↓
Register link
```

Example:

``` text
+----------------------------------+
|              LOGO                |
|                                  |
|        Welcome Back              |
|   Login to continue              |
|                                  |
| Email                            |
| [ user@example.com             ] |
|                                  |
| Password                         |
| [ ***************             ] |
|                                  |
| [ ] Remember me  Forgot password |
|                                  |
| [          LOGIN               ] |
|                                  |
| Don't have an account? Register  |
+----------------------------------+
```

The visual design should remain independent from authentication
implementation.

------------------------------------------------------------------------

# 9. React Concepts Required

Before implementing authentication, understand these React concepts:

## 9.1 Components

React applications are built from reusable components.

``` tsx
function LoginForm() {
  return <form>...</form>;
}
```

------------------------------------------------------------------------

## 9.2 Props

Props allow a parent to pass data to a child.

``` tsx
<PasswordInput
  label="Password"
  placeholder="Enter password"
/>
```

------------------------------------------------------------------------

## 9.3 State

State stores values that can change.

``` tsx
const [email, setEmail] = useState("");
```

Typical login state:

``` text
email
password
loading
error
showPassword
```

React's state model is the foundation for making UI respond to user
input.

------------------------------------------------------------------------

## 9.4 Events

Examples:

``` tsx
onClick
onChange
onSubmit
```

Example:

``` tsx
<form onSubmit={handleSubmit}>
```

------------------------------------------------------------------------

## 9.5 Conditional Rendering

Example:

``` tsx
{loading ? "Logging in..." : "Login"}
```

or:

``` tsx
{error && <p>{error}</p>}
```

------------------------------------------------------------------------

## 9.6 Lists

Backend data is commonly displayed using:

``` tsx
users.map(...)
```

This will become important outside the login feature as well.

------------------------------------------------------------------------

## 9.7 React Router

Routes map URLs to pages.

``` text
/login
/register
/dashboard
```

Conceptually:

``` text
URL
 ↓
Router
 ↓
Page Component
```

------------------------------------------------------------------------

# 10. Login Component State

The login form should initially manage:

``` tsx
const [email, setEmail] = useState("");
const [password, setPassword] = useState("");
const [loading, setLoading] = useState(false);
const [error, setError] = useState("");
const [showPassword, setShowPassword] = useState(false);
```

State responsibilities:

  State          Purpose
  -------------- -----------------------------------------------------
  email          Stores email input
  password       Stores password input
  loading        Prevents duplicate submission and displays progress
  error          Displays authentication/form errors
  showPassword   Toggles password visibility

Do not create duplicate state when the same information can be derived
from existing state.

------------------------------------------------------------------------

# 11. Form Handling

Use a standard HTML form:

``` tsx
<form onSubmit={handleSubmit}>
```

Inputs should be controlled by React state:

``` tsx
<input
  type="email"
  value={email}
  onChange={(e) => setEmail(e.target.value)}
/>
```

Password:

``` tsx
<input
  type={showPassword ? "text" : "password"}
  value={password}
  onChange={(e) => setPassword(e.target.value)}
/>
```

Submit:

``` tsx
<button type="submit">
  Login
</button>
```

------------------------------------------------------------------------

# 12. Client-Side Validation

Before calling the backend:

``` text
Email empty?
    ↓
Show error

Invalid email?
    ↓
Show error

Password empty?
    ↓
Show error

Valid?
    ↓
Call backend
```

Example rules:

``` text
Email:
- Required
- Valid email format

Password:
- Required
- Do not unnecessarily restrict the length on the frontend
```

Important:

Client-side validation improves UX but does NOT replace server-side
validation.

The Java backend must validate the request independently.

------------------------------------------------------------------------

# 13. API Contract

The React frontend and Spring Boot backend must agree on an API
contract.

Recommended starting contract:

## Login

``` text
POST /api/v1/auth/login
```

Request:

``` json
{
  "email": "user@example.com",
  "password": "password"
}
```

The exact response depends on the authentication/session architecture
chosen for the Java backend.

------------------------------------------------------------------------

## Current User

``` text
GET /api/v1/auth/me
```

Purpose:

``` text
Return currently authenticated user
```

Example response:

``` json
{
  "id": "123",
  "name": "Sanket",
  "email": "user@example.com",
  "role": "USER"
}
```

------------------------------------------------------------------------

## Logout

``` text
POST /api/v1/auth/logout
```

The backend should invalidate the authenticated session/token according
to the chosen authentication mechanism.

------------------------------------------------------------------------

## Register

``` text
POST /api/v1/auth/register
```

Request:

``` json
{
  "name": "Sanket",
  "email": "user@example.com",
  "password": "password"
}
```

------------------------------------------------------------------------

## Forgot Password

``` text
POST /api/v1/auth/forgot-password
```

Request:

``` json
{
  "email": "user@example.com"
}
```

------------------------------------------------------------------------

## Reset Password

``` text
POST /api/v1/auth/reset-password
```

Request structure depends on the backend's reset-token design.

------------------------------------------------------------------------

# 14. API Service Layer

Do not scatter `fetch()` calls throughout components.

Create:

``` text
services/
    apiClient.ts
    authService.ts
```

Example:

``` ts
export async function login(
  email: string,
  password: string
) {
  return apiClient.post("/api/v1/auth/login", {
    email,
    password,
  });
}
```

Then:

``` tsx
await login(email, password);
```

This keeps UI and networking separate.

------------------------------------------------------------------------

# 15. Recommended API Client

Create one common API client.

Responsibilities:

``` text
Base URL
↓
HTTP method
↓
Headers
↓
Credentials/session handling
↓
JSON conversion
↓
Error handling
```

Example conceptual usage:

``` ts
apiClient.get("/api/v1/auth/me");

apiClient.post("/api/v1/auth/login", data);

apiClient.post("/api/v1/auth/logout");
```

Do not hardcode environment-specific backend URLs inside components.

Use environment configuration.

Example:

``` text
.env.development
.env.production
```

------------------------------------------------------------------------

# 16. Authentication State

The application needs a central place to know:

``` text
Is the user logged in?
Who is the user?
Is authentication still loading?
How do we log out?
```

Create:

``` text
AuthContext
```

Conceptual state:

``` tsx
{
  user,
  isAuthenticated,
  loading,
  login,
  logout
}
```

Then any relevant component can consume it.

Example:

``` tsx
const {
  user,
  isAuthenticated,
  logout
} = useAuth();
```

------------------------------------------------------------------------

# 17. Application Startup Authentication Check

When the React application starts:

``` text
Application starts
       ↓
Check authentication state
       ↓
GET /api/v1/auth/me
       ↓
       ├── authenticated
       │       ↓
       │    Store user
       │
       └── unauthenticated
               ↓
          user = null
```

This prevents the frontend from assuming that a user is logged in merely
because a previous UI state existed.

------------------------------------------------------------------------

# 18. Protected Routes

Some pages should require authentication.

Example:

``` text
Public:
/
 /login
 /register

Protected:
/dashboard
/profile
/settings
/properties/manage
```

Flow:

``` text
User visits /dashboard
        ↓
Is authenticated?
        |
     ┌──┴──┐
     ↓     ↓
    YES    NO
     ↓     ↓
Dashboard /login
```

Create:

``` text
ProtectedRoute.tsx
```

Conceptual implementation:

``` tsx
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <Loading />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  return children;
}
```

Important:

Frontend protected routes are NOT a security boundary.

The Spring Boot backend must independently verify authentication and
authorization on protected API requests.

------------------------------------------------------------------------

# 19. Login Flow

The final login flow should be:

``` text
User
 ↓
Login Page
 ↓
Enter email/password
 ↓
Client validation
 ↓
setLoading(true)
 ↓
authService.login()
 ↓
POST /api/v1/auth/login
 ↓
Spring Boot
 ↓
Authentication
 ↓
Successful response/session
 ↓
React updates AuthContext
 ↓
Navigate to dashboard
```

Failure:

``` text
Backend rejects login
 ↓
React receives error
 ↓
setLoading(false)
 ↓
Display generic error
```

------------------------------------------------------------------------

# 20. Logout Flow

``` text
User clicks Logout
        ↓
logout()
        ↓
POST /api/v1/auth/logout
        ↓
Backend invalidates session
        ↓
Clear frontend auth state
        ↓
Navigate to /login
```

The backend should perform the actual server-side session termination.

------------------------------------------------------------------------

# 21. Loading States

Every API operation needs a clear loading state.

Example:

``` text
Normal:

[ Login ]

Loading:

[ Logging in... ]
```

Disable duplicate submission:

``` tsx
<button disabled={loading}>
```

Also prevent multiple concurrent login requests from repeated clicks.

------------------------------------------------------------------------

# 22. Error Handling

Handle at least:

``` text
400 → Invalid request
401 → Authentication failed
403 → Not authorized
404 → Resource not found
409 → Business conflict
422 → Validation error, if used by API contract
429 → Too many requests
500 → Server error
Network failure → Backend unreachable
```

The UI should convert technical API failures into understandable
messages.

Do not expose:

``` text
SQL exception
Stack trace
Database error
Internal server path
Authentication implementation details
```

For login failures, prefer a generic message such as:

``` text
Invalid email or password.
```

OWASP recommends generic authentication responses to reduce
account-enumeration risk.

------------------------------------------------------------------------

# 23. Password Security Rules

The React application must NEVER:

-   Store plaintext passwords
-   Log passwords
-   Send passwords anywhere except the intended HTTPS authentication
    endpoint
-   Put passwords in URLs
-   Store passwords in localStorage
-   Store passwords in sessionStorage
-   Print passwords to the console

Passwords must be handled securely by the backend.

OWASP recommends transmitting passwords only over TLS and storing
passwords securely using appropriate password-hashing mechanisms on the
server.

------------------------------------------------------------------------

# 24. Authentication Storage Strategy

The exact storage mechanism will be selected together with the Spring
Boot authentication architecture.

Preferred direction for browser applications:

``` text
Secure server-managed session
or
Secure authentication cookies
```

If cookies are used, security attributes such as:

``` text
Secure
HttpOnly
SameSite
```

must be configured appropriately by the backend.

Do not casually store authentication tokens in:

``` text
localStorage
sessionStorage
```

OWASP's current session-management guidance specifically warns against
storing authentication tokens/session IDs/JWTs in browser storage
accessible to JavaScript and recommends secure HttpOnly cookie
approaches where appropriate.

------------------------------------------------------------------------

# 25. CORS

During development, React and Spring Boot may run on different origins.

Example:

``` text
React:
http://localhost:5173

Spring Boot:
http://localhost:8080
```

The backend must be configured to allow the required frontend origin.

Do not solve CORS by blindly allowing:

``` text
*
```

especially when credentials/cookies are involved.

Configure explicit trusted origins for development and production.

------------------------------------------------------------------------

# 26. Environment Configuration

Do not hardcode:

``` text
http://localhost:8080
```

throughout the code.

Use environment variables.

Example:

``` text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Production:

``` text
VITE_API_BASE_URL=https://api.example.com/api/v1
```

Components should never need to know which environment they are running
in.

------------------------------------------------------------------------

# 27. UI/UX Requirements

The reusable login should support:

-   Desktop
-   Tablet
-   Mobile
-   Keyboard navigation
-   Visible focus states
-   Proper labels
-   Accessible error messages
-   Password manager compatibility
-   Responsive layout
-   Clear loading states
-   Clear error states
-   Clear success feedback

Use semantic HTML:

``` text
<form>
<label>
<input>
<button>
```

rather than building everything with generic `<div>` elements.

------------------------------------------------------------------------

# 28. Reusable Authentication Components

The final reusable components should be approximately:

``` text
AuthLayout
    ↓
LoginForm
    ↓
EmailInput
PasswordInput
RememberMe
ForgotPassword
SubmitButton
RegisterLink
```

The components should accept configuration where appropriate.

For example:

``` tsx
<LoginForm
  title="Welcome Back"
  subtitle="Login to continue"
/>
```

Another project can reuse:

``` tsx
<LoginForm
  title="Admin Login"
  subtitle="Manage your organization"
/>
```

------------------------------------------------------------------------

# 29. Authentication UI vs Business UI

Keep authentication separate from application-specific business
features.

Bad structure:

``` text
Login
 ├── Real estate logic
 ├── Property logic
 ├── Authentication
 └── Dashboard logic
```

Good structure:

``` text
Authentication
    ↓
Reusable auth module

Business application
    ↓
Real estate / e-commerce / SaaS module
```

This allows the authentication frontend to be reused.

------------------------------------------------------------------------

# 30. Development Phases

Do NOT build everything at once.

Follow these phases.

## Phase 1 --- React Setup

Learn/build:

``` text
Vite
React
TypeScript
Folder structure
Components
JSX
```

Deliverable:

``` text
Running React application
```

------------------------------------------------------------------------

## Phase 2 --- Login UI

Build:

``` text
AuthLayout
LoginForm
Email field
Password field
Login button
Forgot password
Register link
```

No backend yet.

Deliverable:

``` text
Professional static login page
```

------------------------------------------------------------------------

## Phase 3 --- React State

Add:

``` text
useState
Email state
Password state
Loading state
Error state
Password visibility
```

Deliverable:

``` text
Fully interactive login form
```

------------------------------------------------------------------------

## Phase 4 --- Validation

Add:

``` text
Required fields
Email validation
Password validation
Error messages
```

Deliverable:

``` text
Validated login form
```

------------------------------------------------------------------------

## Phase 5 --- Mock API

Before Java integration, simulate:

``` text
login()
```

Test:

``` text
success
failure
loading
```

Deliverable:

``` text
Frontend authentication flow without backend
```

------------------------------------------------------------------------

## Phase 6 --- Java Integration

Connect:

``` text
React
 ↓
POST /api/v1/auth/login
 ↓
Spring Boot
```

Deliverable:

``` text
Real login
```

------------------------------------------------------------------------

## Phase 7 --- Authentication Context

Implement:

``` text
AuthContext
useAuth
currentUser
isAuthenticated
login
logout
```

Deliverable:

``` text
Application-wide authentication state
```

------------------------------------------------------------------------

## Phase 8 --- Protected Routes

Implement:

``` text
ProtectedRoute
```

Protect:

``` text
/dashboard
/profile
/settings
```

Deliverable:

``` text
Authenticated application flow
```

------------------------------------------------------------------------

## Phase 9 --- Logout

Implement:

``` text
logout API
↓
clear auth state
↓
redirect to login
```

------------------------------------------------------------------------

## Phase 10 --- Registration

Build:

``` text
/register
```

Connect:

``` text
POST /api/v1/auth/register
```

------------------------------------------------------------------------

## Phase 11 --- Password Recovery

Build:

``` text
/forgot-password
/reset-password
```

Connect to Spring Boot.

------------------------------------------------------------------------

## Phase 12 --- Production Hardening

Review:

``` text
HTTPS
CORS
Cookie/session configuration
Error handling
Rate limiting
Password handling
Authorization
Accessibility
Responsive design
Environment variables
Logging
```

------------------------------------------------------------------------

# 31. Definition of Done

The authentication frontend is considered complete when:

### UI

-   [ ] Login page is responsive
-   [ ] Registration page is responsive
-   [ ] Forgot-password page exists
-   [ ] Password visibility works
-   [ ] Loading state works
-   [ ] Error state works
-   [ ] Accessible labels exist
-   [ ] Keyboard navigation works

### React

-   [ ] Components are reusable
-   [ ] Props are used correctly
-   [ ] State is organized
-   [ ] No unnecessary duplicated state
-   [ ] API logic is separated from UI
-   [ ] Authentication state is centralized
-   [ ] Routes are organized

### API

-   [ ] Login API integrated
-   [ ] Register API integrated
-   [ ] Logout API integrated
-   [ ] Current-user API integrated
-   [ ] Forgot-password API integrated
-   [ ] Reset-password API integrated
-   [ ] API errors handled

### Security

-   [ ] HTTPS used in production
-   [ ] Passwords are never logged
-   [ ] Passwords are never stored by React
-   [ ] Authentication is enforced by backend
-   [ ] Authorization is enforced by backend
-   [ ] Session/token handling follows secure design
-   [ ] CORS is restricted
-   [ ] Generic authentication errors are used
-   [ ] Rate limiting/brute-force protection is handled server-side

------------------------------------------------------------------------

# 32. Development Rules

Follow these rules throughout the project.

## Rule 1

Do not put backend logic inside React.

``` text
React = frontend
Spring Boot = backend
```

## Rule 2

Do not put API calls everywhere.

Use:

``` text
services/
```

## Rule 3

Do not duplicate authentication state.

Use:

``` text
AuthContext
```

when the application reaches the point where shared authentication state
is required.

## Rule 4

Do not trust frontend authorization.

The backend must enforce permissions.

## Rule 5

Do not store passwords.

React only collects the password and sends it securely to the intended
authentication endpoint.

## Rule 6

Do not hardcode API URLs.

Use environment configuration.

## Rule 7

Do not build the entire application in one component.

Prefer:

``` text
Page
 ↓
Components
 ↓
Hooks / Services
```

------------------------------------------------------------------------

# 33. Development Workflow

For every feature:

``` text
1. Understand requirement
        ↓
2. Define UI
        ↓
3. Define component structure
        ↓
4. Define state
        ↓
5. Define API contract
        ↓
6. Build UI
        ↓
7. Add validation
        ↓
8. Connect API
        ↓
9. Handle loading/error/success
        ↓
10. Test
        ↓
11. Refactor
        ↓
12. Document
```

------------------------------------------------------------------------

# 34. Initial API Contract Summary

The first version should plan around:

``` text
POST   /api/v1/auth/login
POST   /api/v1/auth/register
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
```

The exact request/response formats will be finalized when the Spring
Boot backend is implemented.

------------------------------------------------------------------------

# 35. Final Architecture

The final reusable system should look like:

``` text
                         REACT
                           |
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
      Pages           Components           Router
        |                  |                  |
        └──────────────────┼──────────────────┘
                           ↓
                      AuthContext
                           |
                         Hooks
                           |
                       Services
                           |
                      API Client
                           |
                    HTTPS / JSON
                           |
                           ↓
                    SPRING BOOT
                           |
                    Spring Security
                           |
                    Auth Controller
                           |
                     Auth Service
                           |
                    User Repository
                           |
                           ↓
                       DATABASE
```

The key separation is:

``` text
React
→ What the user sees and interacts with

API Service
→ How React communicates with Java

Auth Context
→ What React knows about the current authentication state

Spring Security
→ Whether the user is actually authenticated/authorized

Database
→ Persistent user/application data
```

------------------------------------------------------------------------

# 36. Official References

React documentation: https://react.dev/

React state documentation: https://react.dev/learn/managing-state

OWASP Authentication Cheat Sheet:
https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html

OWASP Session Management Cheat Sheet:
https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html

OWASP Authorization Cheat Sheet:
https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html

These security references should be treated as the baseline when
implementing authentication/session behavior.
