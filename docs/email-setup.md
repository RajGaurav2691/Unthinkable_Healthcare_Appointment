# Email Notification Setup

The application features a transactional email delivery service with built-in retries, fallbacks, and templates.

---

## 1. Providers and Strategy Abstraction

The notification engine is decoupled using the Strategy design pattern:

- **Mock Email Service (`MockEmailService`)**: Active when `EMAIL_ENABLED=false`. Instead of sending real emails (which requires API keys and network costs), it formats the email nicely and logs it to the backend console.
- **SendGrid Email Service (`SendGridEmailService`)**: Active when `EMAIL_ENABLED=true`. Connects to SendGrid’s SMTP API to deliver real emails to users.

---

## 2. Notification Triggers

Transactional emails are automatically dispatched during key lifecycles:

1. **Appointment Confirmation**: Sent to the patient detailing slot date, doctor name, and start time.
2. **Appointment Cancellation**: Sent to both patient and doctor when either cancels the appointment.
3. **Doctor Leave Notification**: Sent to patients whose appointments were automatically cancelled due to a doctor marking leave. Also copies the doctor.
4. **Reminders**: Send daily reminders for appointments scheduled for the following day, as well as medication reminders.

---

## 3. Configuration & Environment Variables

Configure these settings in your `.env` file:

```env
# Set to true to enable real SendGrid mail delivery
EMAIL_ENABLED=false

# SendGrid API Key (obtained from your SendGrid dashboard)
SENDGRID_API_KEY=SG.your_api_key_here

# Verified Sender Email (must be verified in SendGrid Single Sender Verification)
EMAIL_FROM=noreply@yourhealthcareportal.com
```

---

## 4. Retries & Resilience Architecture

All notification dispatches are recorded in the `notifications` database table. If SendGrid is down:
- The appointment booking/cancellation **still succeeds** at the database level.
- The notification record is saved with status `FAILED` or `PENDING` and the exact error description is preserved.
- The `EmailRetryScheduler` runs periodically (every 2 minutes by default) to re-attempt dispatches of failed notifications (up to 3 times before locking the status as permanently `FAILED`).
