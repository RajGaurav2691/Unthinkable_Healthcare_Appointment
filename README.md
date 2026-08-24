# Healthcare Appointment & Follow-up Manager

## Overview
A complete production-style healthcare appointment management system.

## Project Structure
- `backend/`: Spring Boot 3 backend application (Java 17).
- `frontend/`: React 19 frontend application (Vite).
- `docs/`: Architecture and database design documents.

## Environment Variables
Copy `.env.example` to `.env` in the root directory and configure the variables.

## Running the Application

### Backend
1. Ensure PostgreSQL is running.
2. Navigate to `backend/`.
3. Run `mvn spring-boot:run`.

### Frontend
1. Navigate to `frontend/`.
2. Run `npm install`.
3. Run `npm run dev`.

### LLM Configuration
The system features an interchangeable LLM provider architecture for AI summaries. 
- Set `LLM_PROVIDER=mock` to use the fallback mock provider (does not require API keys or network requests).
- Set `LLM_PROVIDER=gemini` to use the real Google Gemini API (requires `LLM_API_KEY`).

```env
POSTGRES_DB=healthcare
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

JWT_SECRET=your_super_secret_jwt_key_that_should_be_very_long

LLM_PROVIDER=mock
LLM_API_KEY=your_gemini_api_key_here
LLM_API_URL=https://generativelanguage.googleapis.com/v1beta
LLM_MODEL=gemini-1.5-flash
```

## Authentication Setup (Phase 2)

### Backend
- JWT Secret Key: The backend uses a 256-bit secure key. You can define this in your environment using the `JWT_SECRET` variable. A default development key is provided in `application.yml`.
- Default Admin Account: Upon startup, the system seeds a default admin account. Configure the credentials via `ADMIN_EMAIL` and `ADMIN_PASSWORD` in your environment variables.
- Endpoints are protected via `JwtAuthenticationFilter` which intercepts the `Authorization: Bearer <token>` header.

### Frontend
- Authentication context (`AuthContext.jsx`) manages the global state and token persistence in `localStorage`.
- Axios is configured (`api/axiosConfig.js`) with an interceptor to automatically attach the token to all API requests and handle `401 Unauthorized` responses gracefully by redirecting to the login page.
- Protected routes use the `ProtectedRoute` and `RoleRoute` components to enforce role-based access to dashboards.
