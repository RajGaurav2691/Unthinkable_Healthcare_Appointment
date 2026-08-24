# System Design Document

This document outlines the core architectural and concurrency patterns implemented in the Healthcare Appointment & Follow-up Manager.

---

## 1. System Architecture
The platform is built as a stateless REST backend coupled to a React frontend. To isolate core database transactions from network instability:
- **Core Core Transactions**: Booking, status updates, and note submissions commit directly to PostgreSQL.
- **Asynchronous Integrations**: Notification dispatches and calendar synchronizations are delegated to Spring `@Async` threads executing outside the database transaction context.

```
Request ──> [Controller] ──> [Service (DB Tx Commit)]
                                  │
                                  ├─Async─> [NotificationService] ──> SendGrid
                                  └─Async─> [CalendarSyncService] ──> Google Calendar
```

---

## 2. E2E Booking & Slot Hold Flow
1. **Slot Inquiry**: Patient views active doctors, select a day, and requests available intervals.
2. **Hold Request**: Patient holds a slot. The system validates availability and saves a temporary `HELD` appointment.
3. **Expiration**: A scheduler runs every minute. Any `HELD` appointment older than 5 minutes is cancelled and returned to the pool.
4. **Confirmation**: Patient submits symptoms. The backend invokes Gemini to generate the pre-visit summary and updates the appointment status to `CONFIRMED`.

---

## 3. Double-Booking & Concurrency Protection
To guarantee a slot is never booked twice under concurrent requests:
1. **Pessimistic Locking**: When querying or holding slots, the doctor's profile is locked using Hibernate `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`), blocking parallel scheduling checks.
2. **Database Constraint**: A unique index acts as the ultimate lock:
   `CREATE UNIQUE INDEX idx_unique_active_appointment ON appointments (doctor_profile_id, appointment_date, start_time) WHERE status IN ('HELD', 'CONFIRMED', 'COMPLETED', 'NO_SHOW');`
   If two threads bypass validation, the database throws a `DataIntegrityViolationException` on the second insert/update, causing a safe transaction rollback.

---

## 4. Doctor Leave & Cancellation Policy
When an Admin registers a doctor's leave:
1. The leave duration is validated and stored.
2. The service scans the `appointments` table for any overlapping confirmed appointments on the leave date.
3. **Cancellation & Alerts**: Overlapping bookings are automatically updated to `CANCELLED`.
4. Asynchronous cancellation emails are immediately queued for both patients and the doctor to preserve schedule visibility.

---

## 5. Resilience Strategies (Fail-Safe Engineering)

### Notification Failure Handling
If SendGrid fails (network drop, API rate limits):
- The appointment **remains confirmed** in the DB.
- The notification record status is set to `FAILED` with the detailed error trace logged.
- The `EmailRetryScheduler` scans failed notifications every 2 minutes and retries dispatches (max 3 attempts).

### LLM Failure Handling
If the Google Gemini API fails during pre-visit or post-visit summary creation:
- The transaction does not abort.
- Original symptoms and clinical notes remain fully preserved.
- A fallback summary is shown to the doctor or patient (e.g., standard text/symptoms string).

### Google Calendar Failure Handling
If Google APIs are offline, or a user revokes permissions:
- The calendar event status is updated to `FAILED` in the database `calendar_events` table along with the error description.
- Booking and notifications are unaffected.
- Synchronization is retried on subsequent status transitions.

---

## 6. Database Design
The schema uses PostgreSQL for operational records. High-frequency queries (filtering appointments, matching user profiles, checking notification statuses) are indexed. Foreign key references enforce referential integrity.

---

## 7. Security
- **Authentication**: Stateless sessions via JWT signed using HMAC-SHA256.
- **Authorization**: Role-Based Access Control (RBAC) enforced at controller boundaries (`@PreAuthorize`).
- **Data Isolation**: Patients cannot access other patients' details, and doctors can only see their assigned appointments.
