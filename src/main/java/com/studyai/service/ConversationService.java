package com.studyai.service;

import com.studyai.entity.ConversationHistory;
import com.studyai.repository.ConversationHistoryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationHistoryRepository repository;

    @Data
    public static class SessionSummary {
        private String sessionId;
        private String title;
        private Integer messageCount;
        private LocalDateTime createdAt;

        public SessionSummary(String sessionId, String title, Integer messageCount, LocalDateTime createdAt) {
            this.sessionId = sessionId;
            this.title = title;
            this.messageCount = messageCount;
            this.createdAt = createdAt;
        }
    }

    /**
     * 获取某个智能体的所有会话列表
     */
    public List<SessionSummary> listSessions(Long studentId, String agentType) {
        List<ConversationHistory> allRecords = repository.findAllByStudentIdAndAgentTypeOrderByCreatedAtAsc(studentId, agentType);

        Map<String, List<ConversationHistory>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(ConversationHistory::getSessionId, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<ConversationHistory> msgs = entry.getValue();
                    String title = msgs.stream()
                            .filter(m -> "user".equals(m.getMessageRole()))
                            .findFirst()
                            .map(m -> {
                                String content = m.getMessageContent();
                                if (content == null || content.isEmpty()) {
                                    return "新建对话";
                                }
                                return content.length() > 30 ? content.substring(0, 30) + "..." : content;
                            })
                            .orElse("新建对话");
                    LocalDateTime createdAt = msgs.get(0).getCreatedAt();
                    return new SessionSummary(entry.getKey(), title, msgs.size(), createdAt);
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 获取某个会话的全部消息
     */
    public List<ConversationHistory> getMessages(Long studentId, String sessionId) {
        return repository.findByStudentIdAndSessionIdOrderByCreatedAtAsc(studentId, sessionId);
    }

    /**
     * 保存一条对话消息
     */
    @Transactional
    public ConversationHistory saveMessage(Long studentId, String sessionId, String role, String content, String agentType) {
        ConversationHistory record = new ConversationHistory();
        record.setStudentId(studentId);
        record.setSessionId(sessionId);
        record.setMessageRole(role);
        record.setMessageContent(content);
        record.setAgentType(agentType);
        return repository.save(record);
    }

    /**
     * 删除某个会话的所有消息
     */
    @Transactional
    public void deleteSession(Long studentId, String sessionId) {
        repository.deleteByStudentIdAndSessionId(studentId, sessionId);
    }
}
