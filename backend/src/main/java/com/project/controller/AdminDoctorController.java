package com.project.controller;

import com.project.dto.request.DoctorCreateRequest;
import com.project.dto.request.DoctorUpdateRequest;
import com.project.dto.request.LeaveRequest;
import com.project.dto.response.DoctorLeaveResponse;
import com.project.dto.response.DoctorResponse;
import com.project.service.DoctorAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final DoctorAdminService doctorAdminService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.ok(doctorAdminService.createDoctor(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorAdminService.getAllDoctors());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorUpdateRequest request) {
        return ResponseEntity.ok(doctorAdminService.updateDoctor(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> updateStatus(@PathVariable Long id, @RequestParam boolean status) {
        return ResponseEntity.ok(doctorAdminService.updateStatus(id, status));
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorLeaveResponse> addLeave(@PathVariable Long id, @Valid @RequestBody LeaveRequest request) {
        return ResponseEntity.ok(doctorAdminService.addLeave(id, request));
    }

    @DeleteMapping("/{id}/leave/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id, @PathVariable Long leaveId) {
        doctorAdminService.deleteLeave(id, leaveId);
        return ResponseEntity.noContent().build();
    }
}
