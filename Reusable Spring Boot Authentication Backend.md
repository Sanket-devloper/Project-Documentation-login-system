# Reusable Spring Boot Authentication Backend
## Frontend-Aligned Development Documentation

---

# 1. Purpose

This document is the backend development guide for building a reusable, professional authentication backend using Java and Spring Boot.

The backend is designed to work directly with the reusable React authentication frontend.

The objective is:

```text
React Frontend
      ↓
REST API Contract
      ↓
Spring Boot Backend
      ↓
Spring Security
      ↓
Authentication / Authorization
      ↓
Service Layer
      ↓
Repository Layer
      ↓
PostgreSQL / MySQL
```

The backend should remain independent from the visual implementation of the frontend while maintaining a stable API contract.

---

# 2. Frontend and Backend Responsibility

The system must maintain a clear separation.

## Frontend Responsibility

```text
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

## Backend Responsibility

```text
HTTP Request
↓
Request validation
↓
Authentication
↓
Authorization
↓
Business logic
↓
Database operations
↓
Security enforcement
↓
HTTP Response
```

The frontend improves the user experience.

The backend is the actual security boundary.

A frontend protected route must never be considered sufficient security.

---

# 3. Technology Stack

Recommended backend stack:

```text
Java 17+
Spring Boot
Spring Web
Spring Security
Spring Data JPA
Hibernate
PostgreSQL
Bean Validation
Maven
Flyway
JUnit 5
Mockito
Spring Boot Test
Testcontainers
```

Optional:

```text
JWT
Mail service
Redis
Docker
OpenAPI / Swagger
SonarCloud
```

---

# 4. Overall Architecture

The complete application should follow:

```text
                         BROWSER
                            |
                            ↓
                     REACT FRONTEND
                            |
                       HTTPS / JSON
                            |
                            ↓
                  SPRING BOOT APPLICATION
                            |
             ┌──────────────┴──────────────┐
             ↓                             ↓
      Spring Security                Controllers
             |                             |
             ↓                             ↓
      Authentication                  Services
             |                             |
             └──────────────┬──────────────┘
                            ↓
                       Repositories
                            |
                            ↓
                         Database
```

The React application must never communicate directly with the database.

---

# 5. Core Backend Layers

Use a layered architecture.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Security sits around the API:

```text
Request
   ↓
Security Filter
   ↓
Authentication
   ↓
Authorization
   ↓
Controller
   ↓
Service
   ↓
Repository
```

---

# 6. Recommended Backend Folder Structure

Recommended structure:

```text
src/main/java/com/example/auth/

├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
│
├── security/
│   ├── AuthenticationFilter.java
│   ├── AuthenticationEntryPoint.java
│   ├── AccessDeniedHandler.java
│   └── SecurityUserDetailsService.java
│
├── auth/
│   ├── controller/
│   │   └── AuthController.java
│   │
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── AuthenticationService.java
│   │   └── PasswordResetService.java
│   │
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── UserResponse.java
│   │   ├── ForgotPasswordRequest.java
│   │   └── ResetPasswordRequest.java
│   │
│   └── mapper/
│       └── AuthMapper.java
│
├── user/
│   ├── entity/
│   │   └── User.java
│   │
│   ├── repository/
│   │   └── UserRepository.java
│   │
│   ├── service/
│   │   └── UserService.java
│   │
│   └── dto/
│       └── UserResponse.java
│
├── role/
│   ├── entity/
│   │   └── Role.java
│   │
│   └── repository/
│       └── RoleRepository.java
│
├── password/
│   ├── entity/
│   │   └── PasswordResetToken.java
│   │
│   └── repository/
│       └── PasswordResetTokenRepository.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UserNotFoundException.java
│   ├── InvalidCredentialsException.java
│   ├── EmailAlreadyExistsException.java
│   └── InvalidResetTokenException.java
│
└── common/
    ├── dto/
    │   ├── ApiResponse.java
    │   └── ErrorResponse.java
    │
    └── constants/
        └── SecurityConstants.java
```

---

# 7. Database Design

The minimum authentication database should contain:

```text
USER
ROLE
USER_ROLE
PASSWORD_RESET_TOKEN
```

Depending on the authentication mechanism:

```text
SESSION
```

or another authentication-related table may also be required.

---

# 8. User Entity

Recommended fields:

```text
User
----------------------------
id
name
email
passwordHash
enabled
createdAt
updatedAt
```

Important:

```text
passwordHash
```

must contain a password hash, never the original password.

Example:

```text
User enters:

mypassword123

        ↓

Backend hashes password

        ↓

Database stores:

<password hash>
```

The backend must never store:

```text
mypassword123
```

---

# 9. Role Design

Authentication and authorization are different.

Authentication:

```text
Who is the user?
```

Authorization:

```text
What is the user allowed to do?
```

Example:

```text
USER
 ├── LOGIN
 ├── VIEW_PROFILE
 └── VIEW_DASHBOARD


ADMIN
 ├── LOGIN
 ├── VIEW_PROFILE
 ├── VIEW_DASHBOARD
 ├── CREATE
 ├── UPDATE
 └── DELETE
```

Backend authorization must be enforced using Spring Security.

---

# 10. Frontend ↔ Backend API Contract

The frontend documentation defines these initial APIs:

```text
POST   /api/v1/auth/login
POST   /api/v1/auth/register
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
```

These endpoints become the primary integration contract.

---

# 11. API 1 — Register

Frontend:

```text
/register
```

calls:

```text
POST /api/v1/auth/register
```

Request:

```json
{
  "name": "Sanket",
  "email": "user@example.com",
  "password": "password"
}
```

Backend flow:

```text
React RegisterForm
        ↓
authService.register()
        ↓
POST /api/v1/auth/register
        ↓
AuthController
        ↓
Validate request
        ↓
Check email uniqueness
        ↓
Hash password
        ↓
Create User
        ↓
Assign default USER role
        ↓
Save User
        ↓
Return response
```

The backend should never return the password.

Example response:

```json
{
  "id": "123",
  "name": "Sanket",
  "email": "user@example.com",
  "role": "USER"
}
```

---

# 12. Registration Validation

Backend validation must happen independently from React validation.

Example:

```text
name
 ├── required
 └── valid length

email
 ├── required
 └── valid email format

password
 └── required
```

Frontend validation:

```text
Improves UX
```

Backend validation:

```text
Security + data integrity
```

Never trust frontend validation.

---

# 13. API 2 — Login

Frontend:

```text
/login
```

calls:

```text
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Backend flow:

```text
React LoginForm
        ↓
Client validation
        ↓
authService.login()
        ↓
POST /api/v1/auth/login
        ↓
Spring Security
        ↓
Find user by email
        ↓
Verify password
        ↓
Check account status
        ↓
Load roles
        ↓
Create authenticated session/token
        ↓
Return authentication result
```

---

# 14. Login Success Flow

```text
User
 ↓
Login Page
 ↓
Email + Password
 ↓
POST /api/v1/auth/login
 ↓
Spring Security
 ↓
Credentials verified
 ↓
Authentication created
 ↓
Secure cookie/session established
 ↓
Response returned
 ↓
React AuthContext updated
 ↓
GET /api/v1/auth/me
 ↓
Current user loaded
 ↓
Navigate /dashboard
```

This aligns with the frontend login flow.

---

# 15. Login Failure Flow

```text
User
 ↓
Login
 ↓
POST /api/v1/auth/login
 ↓
Backend
 ↓
Credentials invalid
 ↓
401 Unauthorized
 ↓
React receives error
 ↓
Display generic message
```

Recommended message:

```text
Invalid email or password.
```

Do not expose:

```text
User does not exist
```

or:

```text
Password is incorrect
```

because these can help account enumeration.

---

# 16. API 3 — Current User

Frontend startup checks:

```text
GET /api/v1/auth/me
```

Purpose:

```text
Return currently authenticated user.
```

Backend flow:

```text
React Application
        ↓
GET /api/v1/auth/me
        ↓
Spring Security
        ↓
Verify authentication
        ↓
Get authenticated principal
        ↓
Find user
        ↓
Map User → UserResponse
        ↓
Return user
```

Example:

```json
{
  "id": "123",
  "name": "Sanket",
  "email": "user@example.com",
  "role": "USER"
}
```

---

# 17. API 4 — Logout

Frontend:

```text
logout()
```

calls:

```text
POST /api/v1/auth/logout
```

Backend flow:

```text
React
 ↓
POST /api/v1/auth/logout
 ↓
Spring Security
 ↓
Invalidate session / authentication
 ↓
Clear authentication cookie if applicable
 ↓
Return 200
 ↓
React clears AuthContext
 ↓
Navigate /login
```

The backend must perform actual authentication termination according to the selected authentication architecture.

---

# 18. API 5 — Forgot Password

Frontend:

```text
/forgot-password
```

calls:

```text
POST /api/v1/auth/forgot-password
```

Request:

```json
{
  "email": "user@example.com"
}
```

Backend flow:

```text
React
 ↓
POST /api/v1/auth/forgot-password
 ↓
Validate email
 ↓
Find user
 ↓
Generate secure reset token
 ↓
Store hashed token
 ↓
Set expiration
 ↓
Send reset email
 ↓
Return generic response
```

Security principle:

Do not reveal whether the email exists.

Example response:

```text
If the account exists, password reset instructions have been sent.
```

---

# 19. API 6 — Reset Password

Frontend:

```text
/reset-password
```

calls:

```text
POST /api/v1/auth/reset-password
```

Possible request:

```json
{
  "token": "reset-token",
  "password": "new-password"
}
```

Backend flow:

```text
React
 ↓
POST /api/v1/auth/reset-password
 ↓
Validate token
 ↓
Check token expiration
 ↓
Find associated user
 ↓
Hash new password
 ↓
Update user password
 ↓
Invalidate reset token
 ↓
Return success
```

---

# 20. Authentication Storage Strategy

For a browser application, the backend should preferably use:

```text
Secure server-managed session
```

or:

```text
Secure authentication cookie
```

If cookies are used, configure:

```text
HttpOnly
Secure
SameSite
```

appropriately.

Avoid designing the system around storing authentication tokens in:

```text
localStorage
sessionStorage
```

unless there is a deliberate security architecture requiring it.

---

# 21. Spring Security Architecture

The security pipeline should conceptually be:

```text
HTTP Request
      ↓
Security Filter Chain
      ↓
Authentication
      ↓
SecurityContext
      ↓
Authorization
      ↓
Controller
```

Public endpoints:

```text
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

Protected endpoint:

```text
GET /api/v1/auth/me
POST /api/v1/auth/logout
```

Business APIs should also be protected according to role/permission requirements.

---

# 22. Authorization

Example:

```text
/api/v1/profile
        ↓
Authenticated users

/api/v1/admin/**
        ↓
ADMIN role
```

Example Spring Security concept:

```text
USER
 ↓
Authenticated
 ↓
Can access user resources

ADMIN
 ↓
Authenticated
 ↓
ADMIN authority
 ↓
Can access admin resources
```

The frontend can hide/show UI based on roles, but the backend must always verify the role.

---

# 23. Controller Layer

Controllers should handle HTTP concerns.

Example responsibility:

```text
AuthController
    ↓
Receive request
    ↓
Validate DTO
    ↓
Call AuthService
    ↓
Return HTTP response
```

Controllers should not contain:

```text
Password hashing
Database queries
Authentication business logic
Email logic
Complex authorization logic
```

Those belong in appropriate services/security components.

---

# 24. Service Layer

The service layer contains business logic.

Example:

```text
AuthService
    ↓
Register user
Login user
Logout user
Get current user
Forgot password
Reset password
```

Conceptual flow:

```text
Controller
    ↓
AuthService
    ↓
UserRepository
    ↓
Database
```

---

# 25. Repository Layer

Repositories handle database access.

Example:

```text
UserRepository
```

Operations:

```text
findByEmail()
existsByEmail()
save()
findById()
```

Repository should not contain authentication UI logic.

---

# 26. DTO Layer

Do not expose JPA entities directly through REST APIs.

Use DTOs.

Example:

```text
LoginRequest
RegisterRequest
LoginResponse
UserResponse
ForgotPasswordRequest
ResetPasswordRequest
```

Flow:

```text
HTTP JSON
 ↓
Request DTO
 ↓
Service
 ↓
Entity
 ↓
Repository
```

Response:

```text
Entity
 ↓
Mapper
 ↓
Response DTO
 ↓
JSON
```

---

# 27. Global Error Handling

Create:

```text
GlobalExceptionHandler
```

It should convert backend exceptions into predictable API responses.

Recommended status codes:

```text
400 → Invalid request
401 → Authentication failed
403 → Not authorized
404 → Resource not found
409 → Business conflict
422 → Validation error
429 → Too many requests
500 → Internal server error
```

Example error response:

```json
{
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid email or password."
}
```

Never return:

```text
SQL exception
Stack trace
Database details
Internal class names
Internal file paths
```

---

# 28. CORS

During development:

```text
React
http://localhost:5173

Spring Boot
http://localhost:8080
```

The backend should explicitly allow the frontend origin.

Example conceptual configuration:

```text
Allowed Origin:
http://localhost:5173
```

Production should use the real frontend origin.

Do not blindly configure:

```text
*
```

especially when cookies/credentials are used.

---

# 29. Environment Configuration

Backend URLs, database credentials, secrets, and security configuration must not be hardcoded.

Example:

```text
application.yml
application-dev.yml
application-prod.yml
```

Environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
AUTH_SECRET
FRONTEND_URL
```

Frontend:

```text
VITE_API_BASE_URL
```

Backend:

```text
FRONTEND_URL
```

This keeps frontend and backend environment configuration independent.

---

# 30. Backend ↔ Frontend Integration Contract

The integration should be treated as a contract.

```text
FRONTEND
   |
   | POST /api/v1/auth/login
   |
   ↓
BACKEND
   |
   | LoginRequest
   |
   ↓
AUTH SERVICE
   |
   ↓
SECURITY
   |
   ↓
DATABASE
```

The frontend should not need to know:

```text
JPA
Hibernate
PostgreSQL
Spring Security
PasswordEncoder
Repository
```

It only needs to know the API contract.

---

# 31. Development Phases

The backend should follow the frontend development phases.

---

## Phase 1 — Spring Boot Setup

Create:

```text
Spring Boot project
Java
Maven
Spring Web
Spring Security
Spring Data JPA
PostgreSQL Driver
Validation
Flyway
```

Deliverable:

```text
Running Spring Boot application
```

---

## Phase 2 — Database Setup

Create:

```text
Database
User table
Role table
User-role relationship
Password reset token table
```

Add Flyway migrations.

Deliverable:

```text
Working database schema
```

---

## Phase 3 — User Domain

Implement:

```text
User Entity
Role Entity
UserRepository
RoleRepository
UserService
```

Add:

```text
UserResponse
```

Deliverable:

```text
Backend can persist and retrieve users.
```

---

## Phase 4 — Registration API

Implement:

```text
POST /api/v1/auth/register
```

Add:

```text
RegisterRequest
Validation
Email uniqueness
Password hashing
Default role
User persistence
```

Deliverable:

```text
React Register page
        ↓
Register API
        ↓
Database
```

---

## Phase 5 — Spring Security

Configure:

```text
SecurityFilterChain
PasswordEncoder
AuthenticationManager
UserDetailsService
AuthenticationEntryPoint
AccessDeniedHandler
CORS
```

Deliverable:

```text
Spring Security protects backend APIs.
```

---

## Phase 6 — Login API

Implement:

```text
POST /api/v1/auth/login
```

Flow:

```text
React Login
 ↓
Backend
 ↓
AuthenticationManager
 ↓
UserDetailsService
 ↓
PasswordEncoder
 ↓
Authentication
 ↓
Session / secure cookie
 ↓
Response
```

Deliverable:

```text
Real frontend login works with Spring Boot.
```

---

## Phase 7 — Current User API

Implement:

```text
GET /api/v1/auth/me
```

Deliverable:

```text
React AuthContext
        ↓
GET /api/v1/auth/me
        ↓
Backend returns current user
```

This directly supports the frontend startup authentication check.

---

## Phase 8 — Protected APIs

Protect:

```text
GET /api/v1/auth/me
POST /api/v1/auth/logout
```

and future business APIs.

Deliverable:

```text
Unauthenticated requests
        ↓
401

Authenticated requests
        ↓
Allowed
```

---

## Phase 9 — Authorization

Add roles:

```text
USER
ADMIN
```

Protect admin APIs.

Example:

```text
/api/v1/admin/**
```

requires:

```text
ROLE_ADMIN
```

Deliverable:

```text
Authentication + authorization working together.
```

---

## Phase 10 — Logout

Implement:

```text
POST /api/v1/auth/logout
```

Flow:

```text
React
 ↓
Logout API
 ↓
Spring Security
 ↓
Invalidate authentication/session
 ↓
Response
 ↓
React clears AuthContext
 ↓
/login
```

---

## Phase 11 — Forgot Password

Implement:

```text
POST /api/v1/auth/forgot-password
```

Build:

```text
PasswordResetToken
Token generation
Expiration
Email delivery
Generic response
```

Deliverable:

```text
Forgot password frontend works with backend.
```

---

## Phase 12 — Reset Password

Implement:

```text
POST /api/v1/auth/reset-password
```

Add:

```text
Token validation
Expiration validation
Password hashing
Password update
Token invalidation
```

Deliverable:

```text
Complete password recovery flow.
```

---

## Phase 13 — Error Handling

Implement:

```text
GlobalExceptionHandler
ErrorResponse
Validation errors
Authentication errors
Authorization errors
Business errors
```

Deliverable:

```text
Frontend receives predictable API errors.
```

---

## Phase 14 — Integration Testing

Test the complete flow.

### Registration

```text
React
 ↓
POST /register
 ↓
Backend
 ↓
Database
```

### Login

```text
React
 ↓
POST /login
 ↓
Spring Security
 ↓
Database
 ↓
Authentication
```

### Current User

```text
React
 ↓
GET /me
 ↓
Security
 ↓
Current User
```

### Logout

```text
React
 ↓
POST /logout
 ↓
Authentication invalidated
```

---

# 32. Complete Frontend + Backend Development Flow

The complete development sequence should now be:

```text
                    REQUIREMENT
                         ↓
                  FRONTEND DESIGN
                         ↓
                 DEFINE API CONTRACT
                         ↓
             ┌───────────┴───────────┐
             ↓                       ↓
        FRONTEND                  BACKEND
             ↓                       ↓
       Component              Controller
             ↓                       ↓
        Form State               DTO
             ↓                       ↓
       Validation               Service
             ↓                       ↓
       API Service              Security
             ↓                       ↓
             └───────────┬───────────┘
                         ↓
                   API INTEGRATION
                         ↓
                  DATABASE TEST
                         ↓
                 INTEGRATION TEST
                         ↓
                   ERROR HANDLING
                         ↓
                     SECURITY
                         ↓
                    REFACTOR
                         ↓
                    DOCUMENT
```

---

# 33. Feature-by-Feature Alignment

Every feature should be developed as a vertical slice.

## Feature: Login

```text
FRONTEND

Login Page
    ↓
LoginForm
    ↓
Validation
    ↓
authService.login()
    ↓
POST /api/v1/auth/login

                ↓

BACKEND

AuthController
    ↓
LoginRequest
    ↓
AuthService
    ↓
Spring Security
    ↓
UserRepository
    ↓
Password verification
    ↓
Authentication
    ↓
Response

                ↓

FRONTEND

AuthContext
    ↓
GET /api/v1/auth/me
    ↓
Store current user
    ↓
Navigate /dashboard
```

---

# 34. Feature: Registration

```text
FRONTEND

Register Page
    ↓
RegisterForm
    ↓
Client Validation
    ↓
authService.register()
    ↓
POST /api/v1/auth/register

                ↓

BACKEND

AuthController
    ↓
RegisterRequest
    ↓
Validation
    ↓
Check email
    ↓
Hash password
    ↓
Create User
    ↓
Assign USER role
    ↓
Save Database
    ↓
UserResponse

                ↓

FRONTEND

Success
    ↓
Navigate to Login
```

---

# 35. Feature: Logout

```text
FRONTEND

Logout Button
    ↓
useAuth().logout()
    ↓
POST /api/v1/auth/logout

                ↓

BACKEND

Spring Security
    ↓
Invalidate authentication/session
    ↓
Return success

                ↓

FRONTEND

Clear AuthContext
    ↓
Navigate /login
```

---

# 36. Feature: Forgot Password

```text
FRONTEND

Forgot Password Page
    ↓
Email
    ↓
POST /api/v1/auth/forgot-password

                ↓

BACKEND

Validate request
    ↓
Find account
    ↓
Generate reset token
    ↓
Store token
    ↓
Send email
    ↓
Generic response

                ↓

USER

Open reset link
    ↓
Reset Password Page
```

---

# 37. Feature: Reset Password

```text
FRONTEND

Reset Password Page
    ↓
Token + New Password
    ↓
POST /api/v1/auth/reset-password

                ↓

BACKEND

Validate token
    ↓
Check expiration
    ↓
Find user
    ↓
Hash password
    ↓
Update password
    ↓
Invalidate token
    ↓
Success

                ↓

FRONTEND

Show success
    ↓
Navigate /login
```

---

# 38. API Contract Table

| Feature | Frontend Page | HTTP Method | Backend Endpoint |
|---|---|---:|---|
| Login | `/login` | POST | `/api/v1/auth/login` |
| Register | `/register` | POST | `/api/v1/auth/register` |
| Current User | Application startup | GET | `/api/v1/auth/me` |
| Logout | Dashboard/Header | POST | `/api/v1/auth/logout` |
| Forgot Password | `/forgot-password` | POST | `/api/v1/auth/forgot-password` |
| Reset Password | `/reset-password` | POST | `/api/v1/auth/reset-password` |

---

# 39. Definition of Done

## Backend Setup

- [ ] Spring Boot application created
- [ ] Maven configured
- [ ] PostgreSQL connected
- [ ] Flyway configured
- [ ] Environment configuration created

## Database

- [ ] User table created
- [ ] Role table created
- [ ] User-role relationship created
- [ ] Password reset token table created
- [ ] Database constraints configured

## Registration

- [ ] Register API implemented
- [ ] Request validation implemented
- [ ] Email uniqueness enforced
- [ ] Password hashing implemented
- [ ] Default USER role assigned

## Authentication

- [ ] Login API implemented
- [ ] Spring Security configured
- [ ] Password verification implemented
- [ ] Authentication/session mechanism implemented
- [ ] Current user API implemented
- [ ] Logout API implemented

## Authorization

- [ ] Roles implemented
- [ ] Protected endpoints implemented
- [ ] Backend authorization enforced
- [ ] 401 handling implemented
- [ ] 403 handling implemented

## Password Recovery

- [ ] Forgot-password API implemented
- [ ] Reset token generated securely
- [ ] Token expiration implemented
- [ ] Reset-password API implemented
- [ ] Token invalidation implemented

## Integration

- [ ] React registration works
- [ ] React login works
- [ ] React `/me` works
- [ ] React logout works
- [ ] React forgot-password works
- [ ] React reset-password works
- [ ] Protected frontend routes work with backend authentication

## Testing

- [ ] Unit tests
- [ ] Controller tests
- [ ] Service tests
- [ ] Repository tests
- [ ] Security tests
- [ ] Integration tests
- [ ] Authentication failure tests
- [ ] Authorization tests
- [ ] Password reset tests

## Security

- [ ] Passwords never logged
- [ ] Passwords never stored in plaintext
- [ ] HTTPS configured for production
- [ ] CORS restricted
- [ ] Secure cookie/session configuration
- [ ] Generic authentication errors
- [ ] Brute-force/rate-limit protection considered
- [ ] Authorization enforced server-side
- [ ] Secrets stored in environment/secret management

---

# 40. Testing Strategy

Testing should follow the same feature flow as development.

```text
Unit Test
    ↓
Controller Test
    ↓
Service Test
    ↓
Security Test
    ↓
Integration Test
    ↓
Frontend + Backend Test
```

Example login integration test:

```text
Create User
    ↓
Hash Password
    ↓
Save Database
    ↓
POST /api/v1/auth/login
    ↓
Authenticate
    ↓
Verify response
    ↓
GET /api/v1/auth/me
    ↓
Verify current user
```

---

# 41. Development Workflow

For every authentication feature:

```text
1. Understand frontend requirement
        ↓
2. Identify frontend page/component
        ↓
3. Define API endpoint
        ↓
4. Define request DTO
        ↓
5. Define response DTO
        ↓
6. Define database requirements
        ↓
7. Implement entity
        ↓
8. Implement repository
        ↓
9. Implement service
        ↓
10. Implement security
        ↓
11. Implement controller
        ↓
12. Implement error handling
        ↓
13. Test backend
        ↓
14. Connect frontend
        ↓
15. Test complete flow
        ↓
16. Refactor
        ↓
17. Document
```

---

# 42. Recommended Development Order

Do not implement everything at once.

Use this exact order:

```text
PHASE 1
Spring Boot Setup
        ↓
PHASE 2
Database + Flyway
        ↓
PHASE 3
User + Role
        ↓
PHASE 4
Registration
        ↓
PHASE 5
Spring Security
        ↓
PHASE 6
Login
        ↓
PHASE 7
/me
        ↓
PHASE 8
Protected APIs
        ↓
PHASE 9
Role-Based Authorization
        ↓
PHASE 10
Logout
        ↓
PHASE 11
Forgot Password
        ↓
PHASE 12
Reset Password
        ↓
PHASE 13
Error Handling
        ↓
PHASE 14
Integration Testing
        ↓
PHASE 15
Security Hardening
```

---

# 43. Frontend and Backend Parallel Development

The frontend and backend can be developed in parallel.

For every feature:

```text
                FEATURE
                   ↓
          Define API Contract
                   ↓
          ┌────────┴────────┐
          ↓                 ↓
      FRONTEND           BACKEND
          ↓                 ↓
     UI Component       Controller
          ↓                 ↓
     Form State             DTO
          ↓                 ↓
     Validation          Service
          ↓                 ↓
     API Service          Security
          ↓                 ↓
          └────────┬────────┘
                   ↓
              Integration
                   ↓
                Testing
```

This prevents frontend and backend from becoming disconnected.

---

# 44. Example Project Structure — Complete

```text
authentication-system/

├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   └── auth/
│   │   ├── pages/
│   │   │   ├── Login.tsx
│   │   │   ├── Register.tsx
│   │   │   ├── ForgotPassword.tsx
│   │   │   ├── ResetPassword.tsx
│   │   │   └── Dashboard.tsx
│   │   ├── context/
│   │   │   └── AuthContext.tsx
│   │   ├── hooks/
│   │   │   └── useAuth.ts
│   │   ├── services/
│   │   │   ├── apiClient.ts
│   │   │   └── authService.ts
│   │   └── routes/
│   │       └── AppRoutes.tsx
│   │
│   └── .env
│
└── backend/
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/example/auth/
    │   │   │
    │   │   └── resources/
    │   │       ├── db/migration/
    │   │       ├── application.yml
    │   │       ├── application-dev.yml
    │   │       └── application-prod.yml
    │   │
    │   └── test/
    │       └── java/
    │
    └── pom.xml
```

---

# 45. Final Architecture

The complete reusable authentication application should look like:

```text
                         USER
                          |
                          ↓
                    REACT FRONTEND
                          |
        ┌─────────────────┼─────────────────┐
        ↓                 ↓                 ↓
      Pages          Components          Router
        |                 |                 |
        └─────────────────┼─────────────────┘
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
                  SPRING BOOT API
                          |
                  Spring Security
                          |
             ┌────────────┴────────────┐
             ↓                         ↓
       AuthController            Other Controllers
             |                         |
        AuthService               Business Services
             |                         |
             └────────────┬────────────┘
                          ↓
                    Repositories
                          |
                          ↓
                      DATABASE
```

---

# 46. Key Architecture Rules

## Rule 1 — React is not the security boundary

```text
React ProtectedRoute
        ≠
Backend Authorization
```

The backend must always verify authentication and authorization.

---

## Rule 2 — Controllers stay thin

```text
Controller
    ↓
Service
```

Do not put business logic into controllers.

---

## Rule 3 — Services contain business logic

```text
Service
    ↓
Repository
```

---

## Rule 4 — Never store plaintext passwords

```text
Password
    ↓
PasswordEncoder
    ↓
Password Hash
    ↓
Database
```

---

## Rule 5 — Frontend and backend communicate through contracts

The frontend should only depend on:

```text
Endpoint
Request
Response
HTTP status
Error contract
```

It should not depend on backend implementation details.

---

## Rule 6 — Authentication and authorization are separate

```text
Authentication
    ↓
Who are you?

Authorization
    ↓
What are you allowed to do?
```

---

## Rule 7 — Build feature-by-feature

Do not build:

```text
Entire frontend
+
Entire backend
+
Entire database
```

and integrate at the end.

Instead:

```text
Registration
    ↓
Frontend + Backend + DB + Test
        ↓
Login
    ↓
Frontend + Backend + DB + Test
        ↓
Current User
    ↓
Frontend + Backend + DB + Test
```

---

# 47. Final Development Philosophy

The project should be developed as a single system with two independently responsible layers.

```text
                    AUTHENTICATION FEATURE
                             |
              ┌──────────────┴──────────────┐
              ↓                             ↓
          FRONTEND                       BACKEND
              |                             |
        User Interface                API Contract
              |                             |
        Form Validation               Request Validation
              |                             |
        API Service                   Controller
              |                             |
        AuthContext                   Service
              |                             |
        ProtectedRoute                Spring Security
              |                             |
              |                       Repository
              |                             |
              └──────────────┬──────────────┘
                             ↓
                         DATABASE
```

The frontend documentation defines **what the user does**.

The backend documentation defines **how the system securely processes what the user does**.

The API contract connects both sides.

Therefore the correct development sequence is:

```text
REQUIREMENT
     ↓
FRONTEND UI
     ↓
API CONTRACT
     ↓
BACKEND IMPLEMENTATION
     ↓
DATABASE
     ↓
SECURITY
     ↓
INTEGRATION
     ↓
TESTING
     ↓
PRODUCTION HARDENING
```

This approach keeps the React and Spring Boot implementations aligned while allowing both sides to evolve independently.