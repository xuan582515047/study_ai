package com.studyai.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyai.dto.PathPlanDTO;
import com.studyai.entity.LearningPath;
import com.studyai.entity.PathStep;
import com.studyai.entity.StudentProfile;
import com.studyai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathPlannerAgent {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是一位资深学习路径规划专家。请根据学生的学习目标、画像和可用时间，生成个性化的学习路径规划。
            输出JSON格式如下：
            {
              "pathName": "路径名称",
              "description": "路径描述",
              "estimatedDuration": 预计总时长(小时),
              "steps": [
                {
                  "stepOrder": 1,
                  "title": "步骤标题",
                  "description": "步骤描述",
                  "stepType": "study/exercise/review/assessment",
                  "durationMinutes": 预计时长(分钟)
                }
              ]
            }
            步骤数量建议在5-15个之间，覆盖从基础到进阶的完整学习过程。
            只输出JSON，不要其他内容。
            """;

    public LearningPath planPath(PathPlanDTO dto, StudentProfile profile) {
        try {
            String userPrompt = buildUserPrompt(dto, profile);
            String jsonResponse = deepSeekService.chatWithSystemPromptJson(SYSTEM_PROMPT, userPrompt);
            JsonNode pathNode = objectMapper.readTree(jsonResponse);

            LearningPath path = new LearningPath();
            path.setStudentId(dto.getStudentId());
            path.setPathName(getText(pathNode, "pathName", dto.getGoal() + "学习路径"));
            path.setDescription(getText(pathNode, "description", ""));
            path.setTargetGoal(dto.getGoal());
            path.setEstimatedDuration(pathNode.path("estimatedDuration").asInt(dto.getDurationDays() != null ? dto.getDurationDays() * 2 : 20));
            path.setStatus("active");
            path.setProgressPercent(0);
            path.setAiPlanJson(jsonResponse);

            log.info("学习路径规划完成, studentId={}, pathName={}", dto.getStudentId(), path.getPathName());
            return path;
        } catch (Exception e) {
            log.error("路径规划失败", e);
            LearningPath fallback = new LearningPath();
            fallback.setStudentId(dto.getStudentId());
            fallback.setPathName(dto.getGoal() + "学习路径");
            fallback.setTargetGoal(dto.getGoal());
            fallback.setStatus("active");
            fallback.setProgressPercent(0);
            return fallback;
        }
    }

    public List<PathStep> extractSteps(String aiPlanJson, Long pathId) {
        List<PathStep> steps = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(aiPlanJson);
            JsonNode stepsNode = root.path("steps");
            if (stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    PathStep step = new PathStep();
                    step.setPathId(pathId);
                    step.setStepOrder(stepNode.path("stepOrder").asInt(1));
                    step.setTitle(getText(stepNode, "title", "学习步骤"));
                    step.setDescription(getText(stepNode, "description", ""));
                    step.setStepType(getText(stepNode, "stepType", "study"));
                    step.setDurationMinutes(stepNode.path("durationMinutes").asInt(60));
                    step.setStatus("pending");
                    steps.add(step);
                }
            }
        } catch (Exception e) {
            log.error("解析路径步骤失败", e);
        }
        return steps;
    }

    private String buildUserPrompt(PathPlanDTO dto, StudentProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("学习目标：").append(dto.getGoal()).append("\n");
        sb.append("学科：").append(dto.getSubject()).append("\n");
        sb.append("可用天数：").append(dto.getDurationDays()).append("天\n");
        if (profile != null) {
            sb.append("学生画像：\n");
            sb.append("- 知识基础：").append(profile.getKnowledgeBase()).append("\n");
            sb.append("- 薄弱点：").append(profile.getWeakPoints()).append("\n");
            sb.append("- 学习节奏：").append(profile.getLearningPace()).append("\n");
        }
        return sb.toString();
    }

    private String getText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asText();
    }
}
