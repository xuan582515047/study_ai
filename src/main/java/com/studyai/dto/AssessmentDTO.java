package com.studyai.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssessmentDTO {
    private Long studentId;
    private String assessmentType;
    private String subject;
    private List<Long> questionIds;
    private List<AnswerDTO> answers;
}
