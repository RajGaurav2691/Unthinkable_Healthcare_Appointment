package com.project.service.calendar;

import com.project.entity.Appointment;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class MockCalendarService implements CalendarService {

    @Override
    public String getAuthUrl(UUID userId) {
        log.info("MockCalendarService: getAuthUrl requested for user {}", userId);
        return "http://localhost:5173/calendar/mock-auth?state=" + userId.toString();
    }

    @Override
    public void handleCallback(String code, String state) {
        log.info("MockCalendarService: handleCallback with code {} and state {}", code, state);
    }

    @Override
    public void disconnect(UUID userId) {
        log.info("MockCalendarService: disconnect requested for user {}", userId);
    }

    @Override
    public boolean isConnected(UUID userId) {
        return true; // Always return true for mock so sync logic runs
    }

    @Override
    public String createEvent(UUID userId, Appointment appointment, String role) {
        String mockEventId = "mock-event-id-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("MockCalendarService: createEvent for user {} ({}) -> Generated Event ID {}", userId, role, mockEventId);
        return mockEventId;
    }

    @Override
    public void updateEvent(UUID userId, String eventId, Appointment appointment, String role) {
        log.info("MockCalendarService: updateEvent {} for user {}", eventId, userId);
    }

    @Override
    public void deleteEvent(UUID userId, String eventId) {
        log.info("MockCalendarService: deleteEvent {} for user {}", eventId, userId);
    }
}
