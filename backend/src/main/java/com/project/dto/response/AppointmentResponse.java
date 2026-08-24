package com.project.dto.response;

import com.project.entity.AppointmentStatus;
import com.project.entity.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private UUID patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private String symptoms;
    private String aiSummary;
    private UrgencyLevel urgencyLevel;
    private String clinicalNotes;
    private String prescription;
    private String aiSuggestedQuestions;
    private String postVisitAiSummary;
    private LocalDateTime createdAt;
}
