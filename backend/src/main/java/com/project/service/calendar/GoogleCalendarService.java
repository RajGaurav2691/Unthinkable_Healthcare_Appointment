package com.project.service.calendar;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.project.entity.Appointment;
import com.project.entity.GoogleCalendarToken;
import com.project.entity.User;
import com.project.repository.GoogleCalendarTokenRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class GoogleCalendarService implements CalendarService {

    private final GoogleCalendarTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${application.google.client-id}")
    private String clientId;

    @Value("${application.google.client-secret}")
    private String clientSecret;

    @Value("${application.google.redirect-uri}")
    private String redirectUri;

    private static final String APPLICATION_NAME = "Healthcare Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events";

    private AuthorizationCodeFlow getFlow() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                httpTransport,
                JSON_FACTORY,
                new GenericUrl("https://oauth2.googleapis.com/token"),
                new BasicAuthentication(clientId, clientSecret),
                clientId,
                "https://accounts.google.com/o/oauth2/v2/auth")
                .setScopes(Collections.singletonList(CALENDAR_SCOPE))
                .build();
    }

    @Override
    public String getAuthUrl(UUID userId) {
        try {
            return getFlow().newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setState(userId.toString())
                    .set("access_type", "offline") // Request refresh token
                    .set("prompt", "consent")        // Force consent screen so refresh_token is always returned
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate Google Calendar auth URL", e);
            throw new RuntimeException("Could not generate auth URL");
        }
    }

    @Override
    @Transactional
    public void handleCallback(String code, String state) {
        try {
            UUID userId = UUID.fromString(state);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));

            TokenResponse response = getFlow().newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            GoogleCalendarToken token = tokenRepository.findByUserId(userId).orElse(new GoogleCalendarToken());
            token.setUser(user);
            token.setAccessToken(response.getAccessToken());
            if (response.getRefreshToken() != null) {
                token.setRefreshToken(response.getRefreshToken());
            }
            token.setExpiresInSeconds(response.getExpiresInSeconds());
            token.setIssuedAt(LocalDateTime.now());

            tokenRepository.save(token);
            log.info("Successfully connected Google Calendar for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to handle Google Calendar callback", e);
            throw new RuntimeException("Failed to connect Google Calendar: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void disconnect(UUID userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("Disconnected Google Calendar for user {}", userId);
    }

    @Override
    public boolean isConnected(UUID userId) {
        return tokenRepository.findByUserId(userId).isPresent();
    }

    private Calendar getCalendarClient(UUID userId) throws GeneralSecurityException, IOException {
        GoogleCalendarToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("User not connected to Google Calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                .setClientAuthentication(new BasicAuthentication(clientId, clientSecret))
                .build();
        
        credential.setAccessToken(token.getAccessToken());
        credential.setRefreshToken(token.getRefreshToken());
        credential.setExpirationTimeMilliseconds(token.getIssuedAt().plusSeconds(token.getExpiresInSeconds()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String createEvent(UUID userId, Appointment appointment, String role) {
        try {
            Calendar service = getCalendarClient(userId);

            Event event = new Event()
                    .setSummary("Medical Appointment with Dr. " + appointment.getDoctor().getUser().getName())
                    .setDescription("Status: " + appointment.getStatus() + "\nPatient: " + appointment.getPatient().getName());

            LocalDateTime start = appointment.getAppointmentDate().atTime(appointment.getStartTime());
            LocalDateTime end = appointment.getAppointmentDate().atTime(appointment.getEndTime());

            DateTime startDateTime = new DateTime(start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            EventDateTime eventStart = new EventDateTime().setDateTime(startDateTime);
            event.setStart(eventStart);

            DateTime endDateTime = new DateTime(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            EventDateTime eventEnd = new EventDateTime().setDateTime(endDateTime);
            event.setEnd(eventEnd);

            Event createdEvent = service.events().insert("primary", event).execute();
            log.info("Created Google Calendar event {} for user {}", createdEvent.getId(), userId);
            return createdEvent.getId();
        } catch (Exception e) {
            log.error("Failed to create Google Calendar event for user {}", userId, e);
            throw new RuntimeException("Failed to create event", e);
        }
    }

    @Override
    public void updateEvent(UUID userId, String eventId, Appointment appointment, String role) {
        try {
            Calendar service = getCalendarClient(userId);

            Event event = service.events().get("primary", eventId).execute();
            event.setSummary("Medical Appointment with Dr. " + appointment.getDoctor().getUser().getName());
            event.setDescription("Status: " + appointment.getStatus() + "\nPatient: " + appointment.getPatient().getName());

            LocalDateTime start = appointment.getAppointmentDate().atTime(appointment.getStartTime());
            LocalDateTime end = appointment.getAppointmentDate().atTime(appointment.getEndTime());

            DateTime startDateTime = new DateTime(start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            event.setStart(new EventDateTime().setDateTime(startDateTime));

            DateTime endDateTime = new DateTime(end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            event.setEnd(new EventDateTime().setDateTime(endDateTime));

            service.events().update("primary", eventId, event).execute();
            log.info("Updated Google Calendar event {} for user {}", eventId, userId);
        } catch (Exception e) {
            log.error("Failed to update Google Calendar event {} for user {}", eventId, userId, e);
            throw new RuntimeException("Failed to update event", e);
        }
    }

    @Override
    public void deleteEvent(UUID userId, String eventId) {
        try {
            Calendar service = getCalendarClient(userId);
            service.events().delete("primary", eventId).execute();
            log.info("Deleted Google Calendar event {} for user {}", eventId, userId);
        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event {} for user {}", eventId, userId, e);
            throw new RuntimeException("Failed to delete event", e);
        }
    }
}
