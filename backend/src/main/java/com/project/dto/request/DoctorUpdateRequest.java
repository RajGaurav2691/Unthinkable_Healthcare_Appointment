package com.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DoctorUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotNull(message = "Experience is required")
    @Min(0)
    private Integer experience;

    @NotNull(message = "Consultation duration is required")
    @Min(5)
    private Integer consultationDuration;

    @NotNull(message = "Active status is required")
    private Boolean activeStatus;

    private List<@Valid WorkingScheduleRequest> schedules;
}
