package com.project.service;

import com.project.dto.request.DoctorCreateRequest;
import com.project.dto.request.DoctorUpdateRequest;
import com.project.dto.request.LeaveRequest;
import com.project.dto.response.DoctorLeaveResponse;
import com.project.dto.response.DoctorResponse;
import com.project.dto.response.WorkingScheduleResponse;
import com.project.entity.DoctorLeave;
import com.project.entity.DoctorProfile;
import com.project.entity.Role;
import com.project.entity.User;
import com.project.entity.WorkingSchedule;
import com.project.repository.DoctorLeaveRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.UserRepository;
import com.project.repository.WorkingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAdminService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final WorkingScheduleRepository workingScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DoctorResponse createDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experience(request.getExperience())
                .consultationDuration(request.getConsultationDuration())
                .activeStatus(true)
                .build();

        final DoctorProfile savedProfile = profile;
        if (request.getSchedules() != null) {
            request.getSchedules().forEach(scheduleReq -> {
                if (scheduleReq.getStartTime().isAfter(scheduleReq.getEndTime()) || scheduleReq.getStartTime().equals(scheduleReq.getEndTime())) {
                    throw new IllegalArgumentException("Start time must be before end time");
                }
                WorkingSchedule ws = WorkingSchedule.builder()
                        .doctorProfile(savedProfile)
                        .dayOfWeek(scheduleReq.getDayOfWeek())
                        .startTime(scheduleReq.getStartTime())
                        .endTime(scheduleReq.getEndTime())
                        .build();
                savedProfile.getSchedules().add(ws);
            });
        }

        profile = doctorProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        profile.getUser().setName(request.getName());
        userRepository.save(profile.getUser());

        profile.setSpecialization(request.getSpecialization());
        profile.setQualification(request.getQualification());
        profile.setExperience(request.getExperience());
        profile.setConsultationDuration(request.getConsultationDuration());
        profile.setActiveStatus(request.getActiveStatus());

        profile.getSchedules().clear();
        final DoctorProfile updatedProfile = profile;
        if (request.getSchedules() != null) {
            request.getSchedules().forEach(scheduleReq -> {
                if (scheduleReq.getStartTime().isAfter(scheduleReq.getEndTime()) || scheduleReq.getStartTime().equals(scheduleReq.getEndTime())) {
                    throw new IllegalArgumentException("Start time must be before end time");
                }
                WorkingSchedule ws = WorkingSchedule.builder()
                        .doctorProfile(updatedProfile)
                        .dayOfWeek(scheduleReq.getDayOfWeek())
                        .startTime(scheduleReq.getStartTime())
                        .endTime(scheduleReq.getEndTime())
                        .build();
                updatedProfile.getSchedules().add(ws);
            });
        }

        profile = doctorProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse updateStatus(Long id, boolean status) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        profile.setActiveStatus(status);
        profile = doctorProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional
    public DoctorLeaveResponse addLeave(Long id, LeaveRequest request) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        
        if (doctorLeaveRepository.existsByDoctorProfileIdAndLeaveDate(id, request.getLeaveDate())) {
            throw new IllegalArgumentException("Leave already exists for this date");
        }

        DoctorLeave leave = DoctorLeave.builder()
                .doctorProfile(profile)
                .leaveDate(request.getLeaveDate())
                .reason(request.getReason())
                .build();
        leave = doctorLeaveRepository.save(leave);

        return DoctorLeaveResponse.builder()
                .id(leave.getId())
                .leaveDate(leave.getLeaveDate())
                .reason(leave.getReason())
                .build();
    }

    @Transactional
    public void deleteLeave(Long doctorId, Long leaveId) {
        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        if (!leave.getDoctorProfile().getId().equals(doctorId)) {
            throw new IllegalArgumentException("Leave does not belong to this doctor");
        }
        doctorLeaveRepository.delete(leave);
    }

    public DoctorResponse mapToResponse(DoctorProfile profile) {
        return DoctorResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId().toString())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .specialization(profile.getSpecialization())
                .qualification(profile.getQualification())
                .experience(profile.getExperience())
                .consultationDuration(profile.getConsultationDuration())
                .activeStatus(profile.isActiveStatus())
                .schedules(profile.getSchedules().stream()
                        .map(s -> WorkingScheduleResponse.builder()
                                .id(s.getId())
                                .dayOfWeek(s.getDayOfWeek())
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .leaves(profile.getLeaves().stream()
                        .map(l -> DoctorLeaveResponse.builder()
                                .id(l.getId())
                                .leaveDate(l.getLeaveDate())
                                .reason(l.getReason())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
