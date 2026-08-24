package com.project.controller;

import com.project.dto.response.AppointmentResponse;
import com.project.dto.request.AppointmentCompleteRequest;
import com.project.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointment(id, authentication.getName()));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentCompleteRequest request, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.markNoShow(id, authentication.getName()));
    }
}
