package com.studyai.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyai.dto.ProfileBuildDTO;
import com.studyai.entity.StudentProfile;
import com.studyai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileBuilderAgent {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是一位专业的教育心理学分析师，擅长通过学生的自我描述精准构建学习画像。
            
            【任务说明】
            请根据学生输入的学习情况描述，从以下8个维度进行深入分析并输出JSON格式结果。
            
            【引导提示词（分析时应考虑）】
            - 年级：学生目前处于哪个学习阶段（如大一/大二/高三等）
            - 专业/学科：学生的主修专业或重点学习的学科领域
            - 学科薄弱点：哪些知识点、题型或概念是学生最容易出错或理解困难的
            - 学习习惯：学生偏好何种学习方式（预习/复习/刷题/看视频/做笔记等）
            - 学习目标：短期和长期的学习目标是什么（考试/竞赛/技能提升/科研等）
            - 学习风格偏好：视觉型/听觉型/动手实践型/阅读型/逻辑推理型
            - 可用时间：每日/每周可投入学习的时间
            - 基础知识掌握程度：对相关前置知识的掌握情况
            
            【输出JSON格式】
            {
              "knowledgeBase": "学生当前知识基础描述，包括已掌握的核心知识和前置知识水平",
              "cognitiveStyle": "认知风格，如视觉型/听觉型/动手型/阅读型/逻辑型，并附简要分析依据",
              "errorPronePoints": "学生容易犯错的知识点和类型，具体到章节或概念",
              "learningGoals": "学习目标描述，区分短期目标和长期目标",
              "majorDirection": "专业方向或学科领域",
              "learningPace": "学习节奏，如快速/适中/慢速/跳跃式，并说明原因",
              "resourcePreference": "资源偏好，如视频/文档/代码/图表/音频/互动练习",
              "weakPoints": "薄弱知识点列表，用逗号分隔，尽量具体"
            }
            
            【要求】
            1. 如果某个维度无法从描述中推断，请填写"待补充"，不要编造。
            2. 分析应尽量深入、具体，避免泛泛而谈。
            3. 只输出JSON，不要输出任何其他解释性文字。
            4. 确保JSON格式合法，字段名严格一致。
            """;

    public StudentProfile buildProfile(ProfileBuildDTO dto) {
        try {
            String jsonResponse = deepSeekService.chatWithSystemPromptJson(SYSTEM_PROMPT, dto.getConversation());
            JsonNode profileNode = objectMapper.readTree(jsonResponse);

            StudentProfile profile = new StudentProfile();
            profile.setStudentId(dto.getStudentId());
            profile.setKnowledgeBase(getText(profileNode, "knowledgeBase"));
            profile.setCognitiveStyle(truncate(getText(profileNode, "cognitiveStyle"), 255));
            profile.setErrorPronePoints(getText(profileNode, "errorPronePoints"));
            profile.setLearningGoals(getText(profileNode, "learningGoals"));
            profile.setMajorDirection(truncate(getText(profileNode, "majorDirection"), 64));
            profile.setLearningPace(truncate(getText(profileNode, "learningPace"), 255));
            profile.setResourcePreference(truncate(getText(profileNode, "resourcePreference"), 255));
            profile.setWeakPoints(getText(profileNode, "weakPoints"));
            profile.setProfileJson(jsonResponse);
            profile.setVersion(1);
            profile.setDeleted(0);

            log.info("学生画像构建完成, studentId={}", dto.getStudentId());
            return profile;
        } catch (Exception e) {
            log.error("画像构建失败", e);
            throw new RuntimeException("画像构建失败: " + e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "待补充" : value.asText();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || "待补充".equals(text)) return text;
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
