package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "quiz_question")
@Where(clause = "deleted = 0")
@SQLDelete(sql = "UPDATE quiz_question SET deleted = 1 WHERE id = ?")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String questionType;
    private String subject;
    private String knowledgePoint;
    private String difficulty;
    private String questionText;
    private String options;
    private String correctAnswer;
    private String explanation;
    private Integer aiGenerated;
    private LocalDateTime createdAt;
    private Integer deleted;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
