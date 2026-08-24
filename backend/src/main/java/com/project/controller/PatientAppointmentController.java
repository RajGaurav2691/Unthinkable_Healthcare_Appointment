package com.project.controller;

import com.project.dto.request.ConfirmRequest;
import com.project.dto.request.HoldRequest;
import com.project.dto.response.AppointmentResponse;
import com.project.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/hold")
    public ResponseEntity<AppointmentResponse> holdSlot(@Valid @RequestBody HoldRequest request, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.holdSlot(request, authentication.getName()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long id, @Valid @RequestBody ConfirmRequest request, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(id, request, authentication.getName()));
    }

    @GetMapping("/patient")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointment(id, authentication.getName()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id, Authentication authentication) {
        appointmentService.cancelAppointment(id, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
