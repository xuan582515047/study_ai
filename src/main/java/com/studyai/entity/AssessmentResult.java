package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "assessment_result")
public class AssessmentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long studentId;
    private String assessmentType;
    private BigDecimal totalScore;
    private BigDecimal maxScore;
    private Integer scorePercent;
    private String masteryLevel;
    private String weakAreas;
    private String strongAreas;
    private String aiAnalysis;
    private String improvementSuggestions;
    private String status;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
