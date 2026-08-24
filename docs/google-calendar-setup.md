# Google Calendar OAuth 2.0 Integration Setup

This guide details configuring and using the Google Calendar API synchronization for patient and doctor dashboards.

---

## 1. Google Cloud Console Setup

### Create a Google Cloud Project
1. Open the [Google Cloud Console](https://console.cloud.google.com).
2. Click the project dropdown in the top navigation bar and select **New Project**.
3. Set the project name (e.g., `Healthcare Manager`) and select/create a billing account if necessary.

### Enable the Google Calendar API
1. Navigate to **APIs & Services → Library** in the sidebar.
2. Search for **Google Calendar API** and select it.
3. Click the **Enable** button.

---

## 2. Configure OAuth Consent Screen

1. Go to **APIs & Services → OAuth consent screen**.
2. Select **External** (unless you are using a workspace directory with internal access restricted).
3. Fill out the mandatory App Information fields:
   - **App name**: Healthcare Manager
   - **User support email**: your-email@gmail.com
   - **Developer contact email**: your-email@gmail.com
4. Click **Save and Continue**.
5. Under **Scopes**, click **Add or Remove Scopes** and add:
   - `https://www.googleapis.com/auth/calendar.events` (View and edit events on all calendars)
6. Under **Test Users**, add the specific Gmail accounts you will use to perform manual E2E tests during development (only these accounts will be allowed to authenticate while the app remains in "Testing" status).

---

## 3. Create OAuth 2.0 Credentials

1. Go to **APIs & Services → Credentials**.
2. Click **Create Credentials** at the top and select **OAuth client ID**.
3. Set the application type to **Web application**.
4. Fill in the **Authorized redirect URIs**:
   - **Local Environment**: `http://localhost:8083/api/calendar/callback`
   - **Production Environment**: `https://your-production-backend-domain.com/api/calendar/callback`
5. Click **Create** and download/save the generated **Client ID** and **Client Secret**.

---

## 4. Environment Configuration

Define these properties in your `.env` file:

```env
# Enable calendar sync (set to false to use Mock mode)
GOOGLE_CALENDAR_ENABLED=true

# Google OAuth Credentials
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8083/api/calendar/callback
```

---

## 5. Token Storage and Refresh Security

- **Database Protection**: The backend persists tokens inside the `google_calendar_tokens` database table. The table is mapped one-to-one with the `users` table.
- **Auto-Refresh**: During Calendar operations, the `GoogleCalendarService` evaluates token validity. If the access token has expired (or is within 60 seconds of expiration), the `Google SDK` dynamically retrieves a fresh access token using the stored `refresh_token` without prompting the user.
- **Unlink / Revoke**: When a user disconnects, all token records are purged from the database, terminating permissions immediately.
