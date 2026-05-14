package com.studyai.repository;

import com.studyai.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByStudentIdAndStatusAndDeleted(Long studentId, Integer status, Integer deleted);

    List<KnowledgeBase> findByStudentIdAndDeleted(Long studentId, Integer deleted);

    Optional<KnowledgeBase> findByIdAndStudentIdAndDeleted(Long id, Long studentId, Integer deleted);

    Optional<KnowledgeBase> findByIdAndDeleted(Long id, Integer deleted);
}
