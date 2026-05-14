package com.studyai.dto;

import lombok.Data;

@Data
public class AnswerDTO {
    private Long questionId;
    private String answer;
    private Integer timeSpentSeconds;
}
