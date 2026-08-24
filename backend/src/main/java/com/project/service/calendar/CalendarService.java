package com.project.service.calendar;

import com.project.entity.Appointment;

import java.util.UUID;

public interface CalendarService {
    String getAuthUrl(UUID userId);
    void handleCallback(String code, String state);
    void disconnect(UUID userId);
    boolean isConnected(UUID userId);
    String createEvent(UUID userId, Appointment appointment, String role);
    void updateEvent(UUID userId, String eventId, Appointment appointment, String role);
    void deleteEvent(UUID userId, String eventId);
}
