package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "learning_path")
@Where(clause = "deleted = 0")
@SQLDelete(sql = "UPDATE learning_path SET deleted = 1 WHERE id = ?")
public class LearningPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long studentId;
    private String pathName;
    private String description;
    private String targetGoal;
    private Integer estimatedDuration;
    private String status;
    private Integer progressPercent;
    private String aiPlanJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
