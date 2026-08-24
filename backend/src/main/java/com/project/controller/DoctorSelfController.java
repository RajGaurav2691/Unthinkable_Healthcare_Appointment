package com.project.controller;

import com.project.dto.request.DoctorUpdateRequest;
import com.project.dto.request.LeaveRequest;
import com.project.dto.response.DoctorLeaveResponse;
import com.project.dto.response.DoctorResponse;
import com.project.entity.DoctorProfile;
import com.project.entity.User;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.UserRepository;
import com.project.service.DoctorAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/doctor/profile")
@RequiredArgsConstructor
public class DoctorSelfController {

    private final DoctorAdminService doctorAdminService;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    private Long getDoctorId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        DoctorProfile profile = doctorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        return profile.getId();
    }

    @GetMapping
    public ResponseEntity<DoctorResponse> getMyProfile(Authentication authentication) {
        Long doctorId = getDoctorId(authentication.getName());
        DoctorProfile profile = doctorProfileRepository.findById(doctorId).orElseThrow();
        return ResponseEntity.ok(doctorAdminService.mapToResponse(profile));
    }

    @PutMapping
    public ResponseEntity<DoctorResponse> updateMyProfile(@Valid @RequestBody DoctorUpdateRequest request, Authentication authentication) {
        Long doctorId = getDoctorId(authentication.getName());
        return ResponseEntity.ok(doctorAdminService.updateDoctor(doctorId, request));
    }

    @PostMapping("/leave")
    public ResponseEntity<DoctorLeaveResponse> addLeave(@Valid @RequestBody LeaveRequest request, Authentication authentication) {
        Long doctorId = getDoctorId(authentication.getName());
        return ResponseEntity.ok(doctorAdminService.addLeave(doctorId, request));
    }

    @DeleteMapping("/leave/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long leaveId, Authentication authentication) {
        Long doctorId = getDoctorId(authentication.getName());
        doctorAdminService.deleteLeave(doctorId, leaveId);
        return ResponseEntity.ok().build();
    }
}
