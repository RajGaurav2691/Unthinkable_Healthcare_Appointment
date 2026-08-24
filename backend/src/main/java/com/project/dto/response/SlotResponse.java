package com.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class SlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // e.g., "AVAILABLE", "BOOKED"
}
