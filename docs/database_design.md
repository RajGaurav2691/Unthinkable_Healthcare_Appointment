# Database Design (Initial Draft)

## Entities

### 1. User (Base for Patient, Doctor, Admin)
- `id` (UUID, PK)
- `email` (String, Unique)
- `password` (String, Hashed)
- `role` (Enum: PATIENT, DOCTOR, ADMIN)
- `created_at` (Timestamp)
- `updated_at` (Timestamp)

### 2. Patient Profile
- `id` (UUID, PK)
- `user_id` (UUID, FK -> User)
- `first_name` (String)
- `last_name` (String)
- `phone_number` (String)

### 3. Doctor Profile
- `id` (UUID, PK)
- `user_id` (UUID, FK -> User)
- `first_name` (String)
- `last_name` (String)
- `specialization` (String)
- `slot_duration_minutes` (Integer)

### 4. Appointment
- `id` (UUID, PK)
- `patient_id` (UUID, FK -> Patient Profile)
- `doctor_id` (UUID, FK -> Doctor Profile)
- `start_time` (Timestamp)
- `end_time` (Timestamp)
- `status` (Enum: PENDING, CONFIRMED, COMPLETED, CANCELLED)
- `symptoms` (Text)
- `ai_pre_visit_summary` (Text)
- `urgency` (Enum: LOW, MEDIUM, HIGH)
- `post_visit_notes` (Text)
- `prescription` (Text)
- `ai_post_visit_summary` (Text)

*(Note: Additional tables for working hours, leave management, and medication schedules will be designed in future phases.)*
