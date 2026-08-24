package com.project.service;

import com.project.dto.response.DoctorResponse;
import com.project.dto.response.SlotResponse;
import com.project.entity.DoctorProfile;
import com.project.entity.WorkingSchedule;
import com.project.repository.DoctorLeaveRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.WorkingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final WorkingScheduleRepository workingScheduleRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final DoctorAdminService doctorAdminService; // used for mapToResponse

    public List<DoctorResponse> getActiveDoctors(String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            return doctorProfileRepository.findByActiveStatusTrueAndSpecializationContainingIgnoreCase(specialization.trim())
                    .stream()
                    .map(doctorAdminService::mapToResponse)
                    .collect(Collectors.toList());
        }
        return doctorProfileRepository.findByActiveStatusTrue()
                .stream()
                .map(doctorAdminService::mapToResponse)
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (!profile.isActiveStatus()) {
            throw new IllegalArgumentException("Doctor is not active");
        }
        return doctorAdminService.mapToResponse(profile);
    }

    public List<SlotResponse> getAvailability(Long doctorId, LocalDate date) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!profile.isActiveStatus()) {
            return new ArrayList<>();
        }

        if (date.isBefore(LocalDate.now())) {
            return new ArrayList<>(); // Never return slots in the past
        }

        boolean isOnLeave = doctorLeaveRepository.existsByDoctorProfileIdAndLeaveDate(doctorId, date);
        if (isOnLeave) {
            return new ArrayList<>();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingSchedule> schedules = workingScheduleRepository.findByDoctorProfileIdAndDayOfWeekOrderByStartTime(doctorId, dayOfWeek);

        List<SlotResponse> availableSlots = new ArrayList<>();
        int duration = profile.getConsultationDuration();

        for (WorkingSchedule schedule : schedules) {
            LocalTime current = schedule.getStartTime();
            while (current.plusMinutes(duration).isBefore(schedule.getEndTime()) || current.plusMinutes(duration).equals(schedule.getEndTime())) {
                
                // Past slots on the same day check
                boolean isPast = date.equals(LocalDate.now()) && current.isBefore(LocalTime.now());
                
                if (!isPast) {
                    availableSlots.add(SlotResponse.builder()
                            .startTime(current)
                            .endTime(current.plusMinutes(duration))
                            .status("AVAILABLE")
                            .build());
                }
                
                current = current.plusMinutes(duration);
            }
        }

        // Future check: Exclude booked appointments here
        return availableSlots;
    }
}
