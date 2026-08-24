package com.project.controller;

import com.project.entity.Appointment;
import com.project.entity.AppointmentStatus;
import com.project.entity.Notification;
import com.project.entity.Role;
import com.project.entity.User;
import com.project.repository.AppointmentRepository;
import com.project.repository.NotificationRepository;
import com.project.repository.UserRepository;
import com.project.service.AdminStatsService;
import com.project.service.DoctorAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminStatsService.getDashboardStats());
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            try {
                AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(appointmentRepository.findByStatusOrderByAppointmentDateDescStartTimeDesc(statusEnum));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(appointmentRepository.findAllByOrderByAppointmentDateDescStartTimeDesc());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllPatients() {
        return ResponseEntity.ok(userRepository.findByRole(Role.PATIENT));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getAllNotifications(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank() && status.equalsIgnoreCase("failed")) {
            return ResponseEntity.ok(notificationRepository.findEligibleForRetry(Integer.MAX_VALUE));
        }
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
