package com.project.service;

import com.project.entity.AppointmentStatus;
import com.project.entity.NotificationStatus;
import com.project.entity.Role;
import com.project.repository.AppointmentRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.NotificationRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;

    public Map<String, Object> getDashboardStats() {
        long totalPatients = userRepository.countByRole(Role.PATIENT);
        long totalDoctors = doctorProfileRepository.count();
        long activeDoctors = doctorProfileRepository.countByActiveStatusTrue();
        long appointmentsToday = appointmentRepository.countByAppointmentDate(LocalDate.now());
        long confirmedUpcoming = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long cancelledTotal = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        long failedNotifications = notificationRepository.countByStatus(NotificationStatus.FAILED);

        return Map.of(
                "totalPatients", totalPatients,
                "totalDoctors", totalDoctors,
                "activeDoctors", activeDoctors,
                "appointmentsToday", appointmentsToday,
                "confirmedUpcoming", confirmedUpcoming,
                "cancelledTotal", cancelledTotal,
                "failedNotifications", failedNotifications
        );
    }
}
