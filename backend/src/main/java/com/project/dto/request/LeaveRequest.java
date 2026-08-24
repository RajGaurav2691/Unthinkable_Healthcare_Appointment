package com.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequest {

    @NotNull(message = "Leave date is required")
    private LocalDate leaveDate;

    private String reason;
}
