package com.studyai.repository;

import com.studyai.entity.PathStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PathStepRepository extends JpaRepository<PathStep, Long> {
    List<PathStep> findByPathIdOrderByStepOrderAsc(Long pathId);
}
