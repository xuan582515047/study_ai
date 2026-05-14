package com.studyai.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResourceGenDTO {
    private Long studentId;
    private List<String> resourceTypes;
    private String subject;
    private String topic;
    private String difficulty;
    private Integer count;
}
