package com.studyai.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyai.service.TutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final TutorService tutorService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            Long studentId = Long.valueOf(payload.get("studentId").toString());
            String sessionId = payload.getOrDefault("sessionId", "").toString();
            String content = payload.get("content").toString();
            String agentType = payload.getOrDefault("agentType", "tutor").toString();

            String response = tutorService.chat(studentId, sessionId, content, agentType);

            Map<String, String> responseMap = Map.of(
                    "type", "message",
                    "content", response,
                    "sessionId", sessionId
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMap)));
        } catch (Exception e) {
            log.error("WebSocket消息处理失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket连接关闭: {}", session.getId());
    }
}
