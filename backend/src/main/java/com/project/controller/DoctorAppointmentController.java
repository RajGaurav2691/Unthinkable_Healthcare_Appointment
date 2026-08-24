package com.project.controller;

import com.project.dto.response.AppointmentResponse;
import com.project.service.AppointmentService;
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
    public ResponseEntity<AppointmentResponse> getAppointmentDetails(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointment(id, authentication.getName()));
    }
}
