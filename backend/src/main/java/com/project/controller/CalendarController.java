package com.project.controller;

import com.project.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.project.repository.UserRepository;
import com.project.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
public class CalendarController {

    private final CalendarService calendarService;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Step 1: Redirect user to Google's OAuth consent screen.
     * Returns the URL the frontend should navigate to.
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect() {
        UUID userId = getCurrentUserId();
        String authUrl = calendarService.getAuthUrl(userId);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * Step 2: Google redirects back to this endpoint with the auth code.
     * This endpoint is PERMITTED WITHOUT JWT — Google doesn't send JWT.
     * The userId is securely embedded in the OAuth 'state' parameter.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        try {
            calendarService.handleCallback(code, state);
            // Redirect to frontend success page
            return ResponseEntity.status(302)
                    .location(URI.create("http://localhost:5173/calendar/success"))
                    .build();
        } catch (Exception e) {
            log.error("Google Calendar OAuth callback failed", e);
            return ResponseEntity.status(302)
                    .location(URI.create("http://localhost:5173/calendar/error"))
                    .build();
        }
    }

    /**
     * Disconnect Google Calendar — revokes stored tokens.
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect() {
        UUID userId = getCurrentUserId();
        calendarService.disconnect(userId);
        return ResponseEntity.ok(Map.of("message", "Google Calendar disconnected successfully."));
    }

    /**
     * Returns whether the current user has connected Google Calendar.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        UUID userId = getCurrentUserId();
        boolean connected = calendarService.isConnected(userId);
        return ResponseEntity.ok(Map.of(
                "connected", connected,
                "userId", userId.toString()
        ));
    }
}
