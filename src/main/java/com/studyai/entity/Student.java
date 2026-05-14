package com.studyai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Data
@Entity
@Table(name = "student")
@Where(clause = "deleted = 0")
@SQLDelete(sql = "UPDATE student SET deleted = 1 WHERE id = ?")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private Integer age;
    private String phone;
    private Integer gender;
    private String major;
    private String school;
    private String grade;
    private Integer status;
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
