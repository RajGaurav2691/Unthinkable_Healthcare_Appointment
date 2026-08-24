# API Documentation

All routes require authentication via JWT unless otherwise specified. Include the token as a header: `Authorization: Bearer <your_jwt_token>`.

---

## 1. Authentication Endpoints

### Register Patient
- **Endpoint**: `POST /api/auth/register`
- **Auth Required**: None
- **Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "securepassword123"
}
```
- **Response**: `200 OK`
```json
{
  "id": "7bc74d82-df76-4d7a-8fba-0dfd8e788bc5",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "role": "PATIENT"
}
```

### User Login
- **Endpoint**: `POST /api/auth/login`
- **Auth Required**: None
- **Request Body**:
```json
{
  "email": "john.doe@example.com",
  "password": "securepassword123"
}
```
- **Response**: `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "7bc74d82-df76-4d7a-8fba-0dfd8e788bc5",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "PATIENT"
  }
}
```

---

## 2. Patient Profile & Booking Endpoints

### Search Active Doctors
- **Endpoint**: `GET /api/doctors`
- **Auth Required**: Yes (`PATIENT`)
- **Query Params**: `specialization` (optional)
- **Response**: `200 OK` (list of doctors)

### Hold Appointment Slot
- **Endpoint**: `POST /api/appointments/hold`
- **Auth Required**: Yes (`PATIENT`)
- **Request Body**:
```json
{
  "doctorId": 1,
  "appointmentDate": "2026-08-25",
  "startTime": "10:00:00",
  "endTime": "10:30:00"
}
```
- **Response**: `200 OK` (creates a `HELD` appointment expiring in 5 minutes)

### Confirm Appointment (Symptom Submission)
- **Endpoint**: `POST /api/appointments/{id}/confirm`
- **Auth Required**: Yes (`PATIENT`)
- **Request Body**:
```json
{
  "symptoms": "Experiencing moderate chest pain and short breath since yesterday morning."
}
```
- **Response**: `200 OK` (AI processing triggered, emails queued, calendar events synced)

### Cancel Appointment
- **Endpoint**: `PATCH /api/appointments/{id}/cancel`
- **Auth Required**: Yes (`PATIENT` or `DOCTOR` or `ADMIN`)
- **Response**: `200 OK`

---

## 3. Doctor Portal Endpoints

### Complete Appointment (Submit Consultation Details)
- **Endpoint**: `POST /api/doctor/appointments/{id}/complete`
- **Auth Required**: Yes (`DOCTOR`)
- **Request Body**:
```json
{
  "clinicalNotes": "Patient has mild hypertension. Advised lifestyle changes and medication.",
  "prescription": "Lisinopril 10mg once daily for 30 days."
}
```
- **Response**: `200 OK` (Generates post-visit patient summary and schedules medications)

---

## 4. Google Calendar OAuth Endpoints

### Get OAuth Link
- **Endpoint**: `GET /api/calendar/connect`
- **Response**:
```json
{
  "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?..."
}
```

### Disconnect Calendar
- **Endpoint**: `POST /api/calendar/disconnect`
- **Response**: `200 OK`

### Calendar Connection Status
- **Endpoint**: `GET /api/calendar/status`
- **Response**:
```json
{
  "connected": true,
  "userId": "7bc74d82-df76-4d7a-8fba-0dfd8e788bc5"
}
```

---

## 5. Admin Portal Endpoints

### Get Dashboard Metrics
- **Endpoint**: `GET /api/admin/stats`
- **Auth Required**: Yes (`ADMIN`)
- **Response**: `200 OK`
```json
{
  "totalPatients": 12,
  "totalDoctors": 4,
  "activeDoctors": 3,
  "appointmentsToday": 5,
  "confirmedUpcoming": 8,
  "cancelledTotal": 2,
  "failedNotifications": 0
}
```
