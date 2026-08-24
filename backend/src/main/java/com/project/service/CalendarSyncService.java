package com.project.service;

import com.project.entity.Appointment;
import com.project.entity.CalendarEvent;
import com.project.repository.CalendarEventRepository;
import com.project.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarSyncService {

    private final CalendarService calendarService;
    private final CalendarEventRepository calendarEventRepository;

    @Async
    @Transactional
    public void syncAppointmentCreation(Appointment appointment) {
        log.info("Starting async sync for appointment creation ID {}", appointment.getId());
        
        CalendarEvent event = calendarEventRepository.findByAppointmentId(appointment.getId())
                .orElse(CalendarEvent.builder().appointment(appointment).build());

        boolean updated = false;
        
        try {
            if (calendarService.isConnected(appointment.getPatient().getId())) {
                String patientEventId = calendarService.createEvent(appointment.getPatient().getId(), appointment, "patient");
                event.setPatientEventId(patientEventId);
                updated = true;
            }
        } catch (Exception e) {
            log.error("Failed to sync calendar for patient of appointment {}", appointment.getId(), e);
            event.setLastError("Patient Sync Error: " + e.getMessage());
            event.setStatus("FAILED");
        }

        try {
            if (calendarService.isConnected(appointment.getDoctor().getUser().getId())) {
                String doctorEventId = calendarService.createEvent(appointment.getDoctor().getUser().getId(), appointment, "doctor");
                event.setDoctorEventId(doctorEventId);
                updated = true;
            }
        } catch (Exception e) {
            log.error("Failed to sync calendar for doctor of appointment {}", appointment.getId(), e);
            event.setLastError("Doctor Sync Error: " + e.getMessage());
            event.setStatus("FAILED");
        }

        if (updated && !event.getStatus().equals("FAILED")) {
            event.setStatus("SYNCED");
            event.setLastError(null);
        }

        calendarEventRepository.save(event);
    }

    @Async
    @Transactional
    public void syncAppointmentCancellation(Appointment appointment) {
        log.info("Starting async sync for appointment cancellation ID {}", appointment.getId());
        
        calendarEventRepository.findByAppointmentId(appointment.getId()).ifPresent(event -> {
            boolean updated = false;

            if (event.getPatientEventId() != null && calendarService.isConnected(appointment.getPatient().getId())) {
                try {
                    calendarService.deleteEvent(appointment.getPatient().getId(), event.getPatientEventId());
                    event.setPatientEventId(null);
                    updated = true;
                } catch (Exception e) {
                    log.error("Failed to delete calendar event for patient of appointment {}", appointment.getId(), e);
                    event.setLastError("Patient Delete Error: " + e.getMessage());
                    event.setStatus("FAILED");
                }
            }

            if (event.getDoctorEventId() != null && calendarService.isConnected(appointment.getDoctor().getUser().getId())) {
                try {
                    calendarService.deleteEvent(appointment.getDoctor().getUser().getId(), event.getDoctorEventId());
                    event.setDoctorEventId(null);
                    updated = true;
                } catch (Exception e) {
                    log.error("Failed to delete calendar event for doctor of appointment {}", appointment.getId(), e);
                    event.setLastError("Doctor Delete Error: " + e.getMessage());
                    event.setStatus("FAILED");
                }
            }

            if (updated && !event.getStatus().equals("FAILED")) {
                event.setStatus("CANCELLED");
                event.setLastError(null);
            }
            
            calendarEventRepository.save(event);
        });
    }
}
