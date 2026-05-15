package com.studyai.repository;

import com.studyai.entity.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {
    List<ConversationHistory> findByStudentIdAndSessionIdOrderByCreatedAtAsc(Long studentId, String sessionId);

    @Query("SELECT c FROM ConversationHistory c WHERE c.studentId = :studentId AND c.agentType = :agentType ORDER BY c.createdAt ASC")
    List<ConversationHistory> findAllByStudentIdAndAgentTypeOrderByCreatedAtAsc(Long studentId, String agentType);

    void deleteByStudentIdAndSessionId(Long studentId, String sessionId);
}
