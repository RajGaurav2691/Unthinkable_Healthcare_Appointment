package com.project.service;

import com.project.entity.*;
import com.project.repository.CalendarEventRepository;
import com.project.service.calendar.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CalendarSyncServiceTest {

    private CalendarService calendarService;
    private CalendarEventRepository calendarEventRepository;
    private CalendarSyncService calendarSyncService;

    private Appointment buildAppointment() {
        User patient = new User();
        patient.setId(UUID.randomUUID());
        patient.setEmail("patient@example.com");
        patient.setName("Test Patient");

        User doctorUser = new User();
        doctorUser.setId(UUID.randomUUID());
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setName("Dr. Test");

        DoctorProfile doctor = new DoctorProfile();
        doctor.setUser(doctorUser);

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointment;
    }

    @BeforeEach
    void setUp() {
        calendarService = mock(CalendarService.class);
        calendarEventRepository = mock(CalendarEventRepository.class);
        calendarSyncService = new CalendarSyncService(calendarService, calendarEventRepository);

        when(calendarEventRepository.findByAppointmentId(anyLong())).thenReturn(Optional.empty());
        when(calendarEventRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void syncAppointmentCreation_bothConnected_createsEvents() {
        Appointment appointment = buildAppointment();
        when(calendarService.isConnected(appointment.getPatient().getId())).thenReturn(true);
        when(calendarService.isConnected(appointment.getDoctor().getUser().getId())).thenReturn(true);
        when(calendarService.createEvent(any(), any(), eq("patient"))).thenReturn("patient-event-123");
        when(calendarService.createEvent(any(), any(), eq("doctor"))).thenReturn("doctor-event-456");

        calendarSyncService.syncAppointmentCreation(appointment);

        verify(calendarService).createEvent(appointment.getPatient().getId(), appointment, "patient");
        verify(calendarService).createEvent(appointment.getDoctor().getUser().getId(), appointment, "doctor");
        verify(calendarEventRepository).save(argThat(e -> 
            "patient-event-123".equals(e.getPatientEventId()) &&
            "doctor-event-456".equals(e.getDoctorEventId())
        ));
    }

    @Test
    void syncAppointmentCreation_neitherConnected_skipsCreation() {
        Appointment appointment = buildAppointment();
        when(calendarService.isConnected(any())).thenReturn(false);

        calendarSyncService.syncAppointmentCreation(appointment);

        verify(calendarService, never()).createEvent(any(), any(), any());
        // Should still save the empty record
        verify(calendarEventRepository).save(any());
    }

    @Test
    void syncAppointmentCreation_googleApiFails_doesNotThrow() {
        Appointment appointment = buildAppointment();
        when(calendarService.isConnected(any())).thenReturn(true);
        when(calendarService.createEvent(any(), any(), any())).thenThrow(new RuntimeException("Google API Error"));

        // Must NOT throw — appointment is already confirmed in DB
        calendarSyncService.syncAppointmentCreation(appointment);

        // CalendarEvent should be saved with FAILED status
        verify(calendarEventRepository).save(argThat(e -> "FAILED".equals(e.getStatus())));
    }

    @Test
    void syncAppointmentCancellation_deletesExistingEvents() {
        Appointment appointment = buildAppointment();

        CalendarEvent existingEvent = new CalendarEvent();
        existingEvent.setAppointment(appointment);
        existingEvent.setPatientEventId("patient-event-123");
        existingEvent.setDoctorEventId("doctor-event-456");
        existingEvent.setStatus("SYNCED");

        when(calendarEventRepository.findByAppointmentId(1L)).thenReturn(Optional.of(existingEvent));
        when(calendarService.isConnected(any())).thenReturn(true);

        calendarSyncService.syncAppointmentCancellation(appointment);

        verify(calendarService).deleteEvent(appointment.getPatient().getId(), "patient-event-123");
        verify(calendarService).deleteEvent(appointment.getDoctor().getUser().getId(), "doctor-event-456");
    }

    @Test
    void syncAppointmentCancellation_noEventRecord_silentlySkips() {
        Appointment appointment = buildAppointment();
        when(calendarEventRepository.findByAppointmentId(anyLong())).thenReturn(Optional.empty());

        // Must NOT throw
        calendarSyncService.syncAppointmentCancellation(appointment);

        verify(calendarService, never()).deleteEvent(any(), any());
    }
}
