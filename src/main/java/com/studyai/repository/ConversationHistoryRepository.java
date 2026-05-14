package com.studyai.repository;

import com.studyai.entity.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {
    List<ConversationHistory> findByStudentIdAndSessionIdOrderByCreatedAtAsc(Long studentId, String sessionId);
}
