package com.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentCompleteRequest {
    @NotBlank(message = "Clinical notes are required")
    private String clinicalNotes;

    @NotBlank(message = "Prescription is required")
    private String prescription;
}
