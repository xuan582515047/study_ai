package com.studyai.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyai.dto.AnswerDTO;
import com.studyai.dto.AssessmentDTO;
import com.studyai.entity.*;
import com.studyai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentAgent {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String QUESTION_GEN_PROMPT = """
            你是一位出题专家。请根据学科和知识点生成测验题目。
            输出JSON数组格式，每道题包含：
            {
              "questionType": "single_choice/multiple_choice/fill_blank/essay/code",
              "questionText": "题目内容",
              "options": {"A": "选项A", "B": "选项B"},
              "correctAnswer": "正确答案",
              "explanation": "解析",
              "difficulty": "easy/medium/hard",
              "knowledgePoint": "知识点"
            }
            生成5道题目，只输出JSON数组，不要其他内容。
            """;

    private static final String ANALYSIS_PROMPT = """
            你是一位学习评估专家。请根据学生的答题情况生成评估报告。
            输出JSON格式：
            {
              "totalScore": 总分,
              "maxScore": 满分,
              "scorePercent": 得分百分比,
              "masteryLevel": "beginner/intermediate/advanced/expert",
              "weakAreas": "薄弱领域",
              "strongAreas": "优势领域",
              "aiAnalysis": "AI分析",
              "improvementSuggestions": "改进建议"
            }
            只输出JSON，不要其他内容。
            """;

    public List<QuizQuestion> generateQuestions(String subject, String knowledgePoint, String difficulty, int count) {
        List<QuizQuestion> questions = new ArrayList<>();
        try {
            String prompt = String.format("学科：%s\n知识点：%s\n难度：%s\n数量：%d", subject, knowledgePoint, difficulty, count);
            String jsonResponse = deepSeekService.chatWithSystemPromptJson(QUESTION_GEN_PROMPT, prompt);
            JsonNode arrayNode = objectMapper.readTree(jsonResponse);
            if (arrayNode.isArray()) {
                for (JsonNode qNode : arrayNode) {
                    QuizQuestion q = new QuizQuestion();
                    q.setQuestionType(getText(qNode, "questionType", "single_choice"));
                    q.setQuestionText(getText(qNode, "questionText", ""));
                    q.setOptions(qNode.path("options").toString());
                    q.setCorrectAnswer(getText(qNode, "correctAnswer", ""));
                    q.setExplanation(getText(qNode, "explanation", ""));
                    q.setDifficulty(getText(qNode, "difficulty", difficulty));
                    q.setKnowledgePoint(getText(qNode, "knowledgePoint", knowledgePoint));
                    q.setSubject(subject);
                    q.setAiGenerated(1);
                    questions.add(q);
                }
            }
        } catch (Exception e) {
            log.error("题目生成失败", e);
        }
        return questions;
    }

    public AssessmentResult analyzeAssessment(AssessmentDTO dto, List<QuizQuestion> questions, List<QuizAttempt> attempts) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("评估类型：").append(dto.getAssessmentType()).append("\n");
            sb.append("答题情况：\n");
            for (int i = 0; i < questions.size() && i < attempts.size(); i++) {
                QuizQuestion q = questions.get(i);
                QuizAttempt a = attempts.get(i);
                sb.append(String.format("题目%d: %s | 学生答案: %s | 正确答案: %s | 是否正确: %s\n",
                        i + 1, q.getQuestionText(), a.getStudentAnswer(), q.getCorrectAnswer(),
                        Boolean.TRUE.equals(a.getIsCorrect() == 1) ? "对" : "错"));
            }

            String jsonResponse = deepSeekService.chatWithSystemPromptJson(ANALYSIS_PROMPT, sb.toString());
            JsonNode resultNode = objectMapper.readTree(jsonResponse);

            AssessmentResult result = new AssessmentResult();
            result.setStudentId(dto.getStudentId());
            result.setAssessmentType(dto.getAssessmentType());
            result.setTotalScore(BigDecimal.valueOf(resultNode.path("totalScore").asDouble(0)));
            result.setMaxScore(BigDecimal.valueOf(resultNode.path("maxScore").asDouble(100)));
            result.setScorePercent(resultNode.path("scorePercent").asInt(0));
            result.setMasteryLevel(getText(resultNode, "masteryLevel", "beginner"));
            result.setWeakAreas(getText(resultNode, "weakAreas", ""));
            result.setStrongAreas(getText(resultNode, "strongAreas", ""));
            result.setAiAnalysis(getText(resultNode, "aiAnalysis", ""));
            result.setImprovementSuggestions(getText(resultNode, "improvementSuggestions", ""));
            result.setStatus("completed");

            return result;
        } catch (Exception e) {
            log.error("评估分析失败", e);
            AssessmentResult fallback = new AssessmentResult();
            fallback.setStudentId(dto.getStudentId());
            fallback.setAssessmentType(dto.getAssessmentType());
            fallback.setStatus("completed");
            return fallback;
        }
    }

    private String getText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asText();
    }
}
