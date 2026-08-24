package com.project.service;

import com.project.dto.request.ConfirmRequest;
import com.project.dto.request.HoldRequest;
import com.project.dto.response.AppointmentResponse;
import com.project.entity.Appointment;
import com.project.entity.AppointmentStatus;
import com.project.entity.DoctorProfile;
import com.project.entity.User;
import com.project.repository.AppointmentRepository;
import com.project.repository.DoctorProfileRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.project.dto.request.AppointmentCompleteRequest;
import com.project.service.LlmIntegrationService.PreVisitLlmResult;
import com.project.dto.request.AppointmentCompleteRequest;

import com.project.entity.MedicationFrequency;
import com.project.entity.MedicationSchedule;
import com.project.entity.NotificationType;
import com.project.repository.MedicationScheduleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityService availabilityService;
    private final LlmIntegrationService llmIntegrationService;
    private final NotificationService notificationService;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AppointmentResponse holdSlot(HoldRequest request, String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        DoctorProfile doctor = doctorRepository.findByIdWithLock(request.getDoctorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        if (!doctor.isActiveStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is currently inactive");
        }

        LocalDate date = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();
        
        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book past time slots");
        }

        // Validate if slot is actually valid for this doctor (working hours + leave)
        boolean isValidSlot = availabilityService.getAvailability(doctor.getId(), date).stream()
                .anyMatch(slot -> slot.getStartTime().equals(startTime));

        if (!isValidSlot) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot is not available or outside working hours/leave");
        }

        // Check if there's already an active appointment (this is a soft check, DB index provides hard guarantee)
        if (appointmentRepository.existsActiveAppointment(doctor.getId(), date, startTime)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot is already booked or held");
        }

        LocalTime endTime = startTime.plusMinutes(doctor.getConsultationDuration());

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.HELD)
                .build();

        try {
            appointment = appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot was booked concurrently by another user");
        }

        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId, ConfirmRequest request, String patientEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getPatient().getEmail().equals(patientEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to confirm this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.HELD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is not in HELD state");
        }

        PreVisitLlmResult llmResult = llmIntegrationService.processSymptoms(request.getSymptoms());

        appointment.setSymptoms(request.getSymptoms());
        appointment.setAiSummary(llmResult.summary());
        appointment.setUrgencyLevel(llmResult.urgencyLevel());
        appointment.setAiSuggestedQuestions(llmResult.rawJson()); // We store the whole raw JSON for easy retrieval of the array
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointment = appointmentRepository.save(appointment);

        String subject = "Appointment Confirmed with Dr. " + appointment.getDoctor().getUser().getName();
        String body = String.format("Dear %s,\n\nYour appointment is confirmed for %s at %s.\n\nThank you.", 
                appointment.getPatient().getName(), appointment.getAppointmentDate(), appointment.getStartTime());
        notificationService.queueNotification(patientEmail, NotificationType.APPOINTMENT_CONFIRMATION, subject, body, appointment);

        return mapToResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String userEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isPatient = appointment.getPatient().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isPatient && !isDoctor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel appointment in state: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        String patientSubject = "Appointment Cancelled";
        String patientBody = String.format("Dear %s,\n\nYour appointment on %s with Dr. %s has been cancelled.", 
                appointment.getPatient().getName(), appointment.getAppointmentDate(), appointment.getDoctor().getUser().getName());
        notificationService.queueNotification(appointment.getPatient().getEmail(), NotificationType.APPOINTMENT_CANCELLATION, patientSubject, patientBody, appointment);

        String doctorSubject = "Appointment Cancelled by Patient";
        String doctorBody = String.format("Dear Dr. %s,\n\nThe appointment on %s at %s with patient %s has been cancelled.",
                appointment.getDoctor().getUser().getName(), appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getPatient().getName());
        notificationService.queueNotification(appointment.getDoctor().getUser().getEmail(), NotificationType.APPOINTMENT_CANCELLATION, doctorSubject, doctorBody, appointment);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, AppointmentCompleteRequest request, String doctorEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getDoctor().getUser().getEmail().equals(doctorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to complete this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED appointments can be completed");
        }

        String postVisitSummary = llmIntegrationService.generatePostVisitSummary(request.getClinicalNotes(), request.getPrescription());

        appointment.setClinicalNotes(request.getClinicalNotes());
        appointment.setPrescription(request.getPrescription());
        appointment.setPostVisitAiSummary(postVisitSummary);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        appointment = appointmentRepository.save(appointment);

        // Parse medications and create MedicationSchedule records
        try {
            JsonNode root = objectMapper.readTree(postVisitSummary);
            if (root.has("medications") && root.get("medications").isArray()) {
                for (JsonNode medNode : root.get("medications")) {
                    String medName = medNode.has("name") ? medNode.get("name").asText() : "Unknown";
                    String medDosage = medNode.has("dosage") ? medNode.get("dosage").asText() : "As directed";
                    
                    MedicationSchedule schedule = MedicationSchedule.builder()
                            .patient(appointment.getPatient())
                            .appointment(appointment)
                            .medicationName(medName)
                            .dosage(medDosage)
                            .frequency(MedicationFrequency.CUSTOM) // Default to custom, dosage has full instructions
                            .active(true)
                            .startDate(LocalDate.now().atStartOfDay())
                            .build();
                    medicationScheduleRepository.save(schedule);
                }
            }
        } catch (Exception e) {
            // Ignore parse errors, just don't schedule automated reminders
        }

        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse markNoShow(Long appointmentId, String doctorEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getDoctor().getUser().getEmail().equals(doctorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to modify this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only CONFIRMED appointments can be marked as NO_SHOW");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment = appointmentRepository.save(appointment);
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getPatientAppointments(String email) {
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patient.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorAppointments(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        DoctorProfile doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctor.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public AppointmentResponse getAppointment(Long id, String email) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isPatient = appointment.getPatient().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isPatient && !isDoctor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this appointment");
        }

        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .symptoms(appointment.getSymptoms())
                .aiSummary(appointment.getAiSummary())
                .urgencyLevel(appointment.getUrgencyLevel())
                .clinicalNotes(appointment.getClinicalNotes())
                .prescription(appointment.getPrescription())
                .aiSuggestedQuestions(appointment.getAiSuggestedQuestions())
                .postVisitAiSummary(appointment.getPostVisitAiSummary())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
