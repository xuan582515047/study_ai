package com.studyai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentRequestDTO {
    private String input;
    private String imageBase64;
    private String context;
    private Long studentId;
    private Long knowledgeBaseId;
    private List<Map<String, Object>> messages;
}
