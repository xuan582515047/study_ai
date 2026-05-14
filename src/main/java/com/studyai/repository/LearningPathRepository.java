package com.studyai.repository;

import com.studyai.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    List<LearningPath> findByStudentId(Long studentId);
}
