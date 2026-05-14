package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "learning_resource")
@Where(clause = "deleted = 0")
@SQLDelete(sql = "UPDATE learning_resource SET deleted = 1 WHERE id = ?")
public class LearningResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long studentId;
    private String resourceType;
    private String title;
    private String content;
    private String contentUrl;
    private String tags;
    private String difficultyLevel;
    private String subject;
    private String knowledgePoints;
    private String aiModel;
    private String generationParams;
    private String status;
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
