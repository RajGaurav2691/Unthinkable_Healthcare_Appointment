# Architecture Overview

## 1. System Components
- **Frontend**: Single Page Application (SPA) built with React 19, Vite, and React Router.
- **Backend**: REST API built with Spring Boot 3 and Java 17.
- **Database**: PostgreSQL for relational data storage.

## 2. Request Flow
1. User interacts with the Frontend (React SPA).
2. Frontend makes HTTP requests via Axios to the Backend REST API.
3. Controller layer intercepts the request, validates input DTOs, and delegates to the Service layer.
4. Service layer applies business logic and uses the Repository layer for data access.
5. Repository layer interacts with PostgreSQL via Spring Data JPA.
6. The flow reverses, mapping Entities to response DTOs to return to the Frontend.

## 3. Package Structure (Backend)
- `controller`: REST API endpoints.
- `service`: Business logic interfaces and implementations.
- `repository`: Spring Data JPA interfaces.
- `entity`: JPA domain models.
- `dto`: Data Transfer Objects for API requests and responses.
- `exception`: Global exception handling and custom exceptions.
- `security`: Authentication, authorization, and CORS configurations.
- `mapper`: Mapping between Entities and DTOs.
- `scheduler`: Background tasks.
- `integration`: External services (LLM, Email, Calendar).
- `util`: Utility classes.

## 4. External Integrations (Planned)
- **LLM**: For generating pre-visit symptom summaries and post-visit patient-friendly summaries.
- **Email**: For booking confirmations and reminders.
- **Google Calendar**: For syncing appointments.
