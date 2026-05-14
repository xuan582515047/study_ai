package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "path_step")
public class PathStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pathId;
    private Integer stepOrder;
    private String title;
    private String description;
    private Long resourceId;
    private String stepType;
    private String status;
    private Integer durationMinutes;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
