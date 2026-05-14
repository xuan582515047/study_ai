package com.studyai.dto;

import lombok.Data;

@Data
public class PathPlanDTO {
    private Long studentId;
    private String goal;
    private String subject;
    private Integer durationDays;
}
