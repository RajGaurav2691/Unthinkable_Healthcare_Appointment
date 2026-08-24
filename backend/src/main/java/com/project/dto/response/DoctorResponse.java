package com.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String userId; // UUID as String
    private String name;
    private String email;
    private String specialization;
    private String qualification;
    private Integer experience;
    private Integer consultationDuration;
    private boolean activeStatus;
    private List<WorkingScheduleResponse> schedules;
    private List<DoctorLeaveResponse> leaves;
}
