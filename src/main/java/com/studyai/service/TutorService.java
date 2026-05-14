package com.studyai.service;

import com.studyai.entity.ConversationHistory;
import com.studyai.entity.StudentProfile;
import com.studyai.repository.ConversationHistoryRepository;
import com.studyai.service.agent.TutorAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final ConversationHistoryRepository historyRepository;
    private final TutorAgent tutorAgent;
    private final StudentProfileService profileService;

    @Transactional
    public String chat(Long studentId, String sessionId, String question, String agentType) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        List<ConversationHistory> history = historyRepository.findByStudentIdAndSessionIdOrderByCreatedAtAsc(studentId, sessionId);

        ConversationHistory userMsg = new ConversationHistory();
        userMsg.setStudentId(studentId);
        userMsg.setSessionId(sessionId);
        userMsg.setMessageRole("user");
        userMsg.setMessageContent(question);
        userMsg.setAgentType(agentType);
        historyRepository.save(userMsg);

        StudentProfile profile = profileService.getLatestProfile(studentId);
        String response = tutorAgent.tutorResponse(question, profile, history);

        ConversationHistory assistantMsg = new ConversationHistory();
        assistantMsg.setStudentId(studentId);
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setMessageRole("assistant");
        assistantMsg.setMessageContent(response);
        assistantMsg.setAgentType(agentType);
        historyRepository.save(assistantMsg);

        return response;
    }

    public List<ConversationHistory> getHistory(Long studentId, String sessionId) {
        return historyRepository.findByStudentIdAndSessionIdOrderByCreatedAtAsc(studentId, sessionId);
    }
}
