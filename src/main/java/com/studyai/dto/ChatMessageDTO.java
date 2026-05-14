package com.studyai.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long studentId;
    private String sessionId;
    private String content;
    private String agentType;
}
