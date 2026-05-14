package com.studyai.repository;

import com.studyai.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    StudentProfile findTopByStudentIdOrderByVersionDesc(Long studentId);
}
