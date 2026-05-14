package com.studyai.service.agent;

import com.studyai.entity.ConversationHistory;
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
public class TutorAgent {

    private final DeepSeekService deepSeekService;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一位耐心、专业的AI学习辅导老师。你的职责是：
            1. 根据学生的问题提供清晰、准确的解答
            2. 针对学生的薄弱知识点进行重点讲解
            3. 使用学生偏好的认知风格进行教学
            4. 鼓励学生，保持积极的学习氛围
            5. 当学生理解困难时，提供类比、示例和可视化解释
            
            学生画像信息：
            {profile_info}
            
            请以友好、专业的语气回答学生的问题。
            """;

    public String tutorResponse(String question, StudentProfile profile, List<ConversationHistory> history) {
        String profileInfo = buildProfileInfo(profile);
        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.replace("{profile_info}", profileInfo);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        for (ConversationHistory h : history) {
            messages.add(Map.of("role", h.getMessageRole(), "content", h.getMessageContent()));
        }
        messages.add(Map.of("role", "user", "content", question));

        try {
            return deepSeekService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("辅导回答生成失败", e);
            return "抱歉，我暂时无法回答这个问题，请稍后再试。";
        }
    }

    private String buildProfileInfo(StudentProfile profile) {
        if (profile == null) return "暂无学生画像信息";
        StringBuilder sb = new StringBuilder();
        sb.append("- 知识基础：").append(profile.getKnowledgeBase()).append("\n");
        sb.append("- 认知风格：").append(profile.getCognitiveStyle()).append("\n");
        sb.append("- 薄弱点：").append(profile.getWeakPoints()).append("\n");
        sb.append("- 学习目标：").append(profile.getLearningGoals()).append("\n");
        sb.append("- 资源偏好：").append(profile.getResourcePreference()).append("\n");
        return sb.toString();
    }
}
