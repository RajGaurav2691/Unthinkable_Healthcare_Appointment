# Deployment Guide

This guide covers deploying the Healthcare Appointment & Follow-up Manager to production environments.

---

## 1. Environment Variable Checklist

Both backend and database systems require specific configuration inputs. Ensure the following environment variables are set in your production console (e.g., Render, Railway, Vercel):

| Variable Name | Purpose | Example / Note |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Force production configuration profiles | `prod` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection link | `jdbc:postgresql://<host>:<port>/<db>` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `production_db_password_here` |
| `JWT_SECRET` | Secure key for validating user sessions | Generate 256-bit hash key |
| `LLM_PROVIDER` | AI summarization engine | `gemini` (production) / `mock` (local) |
| `LLM_API_KEY` | Google Gemini API key | `AIzaSy...` |
| `EMAIL_ENABLED` | Toggle email notification delivery | `true` |
| `SENDGRID_API_KEY` | SendGrid SMTP credentials | `SG.your_api_key_here` |
| `EMAIL_FROM` | Verified sender domain email | `noreply@yourdomain.com` |
| `GOOGLE_CALENDAR_ENABLED` | Toggle real Google Calendar integration | `true` |
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID | `...apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret | `your-secret-key` |
| `GOOGLE_REDIRECT_URI` | Production Callback URL | `https://your-backend.com/api/calendar/callback` |

---

## 2. Backend Deployment (e.g., Render or Railway)

1. Connect your GitHub repository to **Render** or **Railway**.
2. Select **Web Service** (Render) or **New Service** (Railway).
3. Set the build and run commands:
   - **Root directory**: `backend`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/healthcare-manager-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
4. Add all environment variables from the checklist above.

---

## 3. Database Deployment (e.g., Hosted PostgreSQL)

1. Provision a hosted PostgreSQL database (using Supabase, Render PostgreSQL, AWS RDS, or Neon).
2. Configure the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` on the backend service to point to the newly created instance.
3. The schema will be automatically generated upon startup via Hibernate's validation configuration (`spring.jpa.hibernate.ddl-auto=update`) or Flyway migrations.

---

## 4. Frontend Deployment (e.g., Vercel)

1. Connect your GitHub repository to **Vercel**.
2. Configure project options:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
3. Set the environment variable `VITE_API_URL` to point to your deployed backend URL:
   - `VITE_API_URL=https://your-backend-service.onrender.com`
4. Deploy the application.

---

## 5. Production CORS Configuration

Ensure CORS permissions in `SecurityConfig.java` match your production URL to avoid cross-origin request blockages:

```java
// Configured inside securityFilterChain or corsConfigurationSource:
configuration.setAllowedOrigins(List.of("https://your-frontend-vercel-domain.vercel.app"));
```
