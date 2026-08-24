package com.project.scheduler;

import com.project.entity.Appointment;
import com.project.entity.MedicationSchedule;
import com.project.entity.NotificationType;
import com.project.repository.AppointmentRepository;
import com.project.repository.MedicationScheduleRepository;
import com.project.repository.NotificationRepository;
import com.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void generateAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> upcoming = appointmentRepository.findConfirmedAppointmentsForDate(tomorrow);

        log.info("Checking appointment reminders for {} (Found: {})", tomorrow, upcoming.size());

        for (Appointment appt : upcoming) {
            // Check if reminder already generated for this appointment
            boolean alreadySent = notificationRepository.existsByRelatedAppointmentIdAndType(appt.getId(), NotificationType.APPOINTMENT_REMINDER);
            if (!alreadySent) {
                String subject = "Reminder: Upcoming Appointment with Dr. " + appt.getDoctor().getUser().getName();
                String body = String.format("Dear %s,\n\nThis is a reminder for your upcoming appointment on %s at %s.\n\nPlease arrive 10 minutes early.\n\nThank you.",
                        appt.getPatient().getName(), appt.getAppointmentDate(), appt.getStartTime());
                
                notificationService.queueNotification(appt.getPatient().getEmail(), NotificationType.APPOINTMENT_REMINDER, subject, body, appt);
            }
        }
    }

    // Run daily at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void generateMedicationReminders() {
        List<MedicationSchedule> activeSchedules = medicationScheduleRepository.findByActiveTrue();
        
        log.info("Generating daily medication reminders for {} active schedules", activeSchedules.size());
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);

        for (MedicationSchedule schedule : activeSchedules) {
            boolean alreadySentToday = notificationRepository.existsByRecipientAndTypeSince(
                    schedule.getPatient().getEmail(), NotificationType.MEDICATION_REMINDER, startOfDay);

            if (!alreadySentToday) {
                String subject = "Daily Medication Reminder";
                String body = String.format("Dear %s,\n\nPlease remember to take your medication today:\nMedication: %s\nDosage: %s\nFrequency: %s\n\nStay healthy!",
                        schedule.getPatient().getName(), schedule.getMedicationName(), schedule.getDosage(), schedule.getFrequency());

                notificationService.queueNotification(schedule.getPatient().getEmail(), NotificationType.MEDICATION_REMINDER, subject, body, schedule.getAppointment());
            }
        }
    }
}
