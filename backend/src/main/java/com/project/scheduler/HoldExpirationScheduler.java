package com.project.scheduler;

import com.project.entity.Appointment;
import com.project.entity.AppointmentStatus;
import com.project.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpirationScheduler {

    private final AppointmentRepository appointmentRepository;

    @Scheduled(fixedRate = 60000) // Run every 1 minute
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(10);
        List<Appointment> expiredHolds = appointmentRepository.findExpiredHolds(AppointmentStatus.HELD, expiryTime);

        if (!expiredHolds.isEmpty()) {
            log.info("Found {} expired holds to release", expiredHolds.size());
            for (Appointment appointment : expiredHolds) {
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(appointment);
                log.info("Released hold for appointment ID: {}", appointment.getId());
            }
        }
    }
}
