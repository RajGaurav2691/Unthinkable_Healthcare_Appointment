package com.project.controller;

import com.project.dto.response.DoctorResponse;
import com.project.dto.response.SlotResponse;
import com.project.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class PatientDoctorController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getDoctors(@RequestParam(required = false) String specialization) {
        return ResponseEntity.ok(doctorAvailabilityService.getActiveDoctors(specialization));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorAvailabilityService.getDoctorById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponse>> searchDoctors(@RequestParam String specialization) {
        return ResponseEntity.ok(doctorAvailabilityService.getActiveDoctors(specialization));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<SlotResponse>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorAvailabilityService.getAvailability(id, date));
    }
}
