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

# Phase 7: Email Configuration
# Set EMAIL_ENABLED to false for local dev (logs emails instead of sending)
EMAIL_ENABLED=false
SENDGRID_API_KEY=your_sendgrid_api_key
EMAIL_FROM=noreply@example.com
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

---

## Phase 8: Google Calendar OAuth 2.0 Integration

Google Calendar is integrated so that patients and doctors automatically get calendar events when appointments are booked, and events are updated or removed on reschedule/cancel.

> **Important:** Google Calendar failures will **never** cause appointment booking to fail. Calendar sync is performed asynchronously after the database transaction commits.

### Local Development (Mock Mode)

By default \GOOGLE_CALENDAR_ENABLED=false\ uses \MockCalendarService\. All calendar operations are logged to the console without hitting any external API.

### Google Cloud Setup Steps

1. **Create Google Cloud Project** at [console.cloud.google.com](https://console.cloud.google.com)
2. **Enable Google Calendar API**: APIs and Services -> Library -> Search 'Google Calendar API' -> Enable
3. **OAuth Consent Screen**: APIs and Services -> OAuth consent screen -> External -> Add scope \https://www.googleapis.com/auth/calendar.events\`n4. **Create OAuth 2.0 Credentials**: APIs and Services -> Credentials -> Create -> OAuth client ID -> Web application
   - Authorized redirect URI (local): \http://localhost:8083/api/calendar/callback\`n   - Authorized redirect URI (prod): \https://your-domain.com/api/calendar/callback\`n5. **Set env variables:**
```env
GOOGLE_CALENDAR_ENABLED=true
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8083/api/calendar/callback
```
