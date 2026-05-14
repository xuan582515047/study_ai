package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "student_profile")
@Where(clause = "deleted = 0")
@SQLDelete(sql = "UPDATE student_profile SET deleted = 1 WHERE id = ?")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long studentId;
    private String knowledgeBase;
    private String cognitiveStyle;
    private String errorPronePoints;
    private String learningGoals;
    private String majorDirection;
    private String learningPace;
    private String resourcePreference;
    private String weakPoints;
    private String profileJson;
    private Integer version;
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
