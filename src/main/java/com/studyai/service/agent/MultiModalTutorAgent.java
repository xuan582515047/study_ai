package com.studyai.service.agent;

import com.studyai.dto.AgentRequestDTO;
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
public class MultiModalTutorAgent {

    private final DeepSeekService deepSeekService;

    private static final String SYSTEM_PROMPT = """
            你是StudyAI系统的【多模态问答导师】，专注于为学生提供结构化、深度的学科问题解答。
            
            【核心能力】
            1. 支持文字+图片输入的问题分析
            2. 提供结构化解答：问题分析 → 解题过程 → 最终结论 → 易错提醒
            3. 在解答中建议图解方式（如流程图、示意图的文字描述）
            4. 智能追问：在解答末尾提出1-2个引导性问题，帮助学生深化理解
            5. 标记薄弱点：明确指出本题涉及的学生可能存在的知识薄弱点
            
            【输出格式要求】
            请按以下结构输出：
            
            ## 问题分析
            （分析题目考查的知识点和关键思路）
            
            ## 解题过程
            （详细、分步骤的解答过程）
            
            ## 结论
            （最终答案或核心观点总结）
            
            ## 易错提醒
            （列出本题常见的错误思路和注意事项）
            
            ## 图解建议
            （用文字描述可以如何画图辅助理解）
            
            ## 智能追问
            1. （追问问题1）
            2. （追问问题2）
            
            ## 关联薄弱点
            （指出本题涉及的知识点中，学生可能存在的薄弱点，用于同步给其他智能体）
            
            【原则】
            - 解答必须准确、严谨，适合学生理解水平
            - 鼓励启发式教学，不要直接给答案，要展示思考过程
            - 如果用户提供了图片，请结合图片内容进行分析
            """;

    public String tutor(AgentRequestDTO dto) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String userContent = dto.getInput();
            if (dto.getContext() != null && !dto.getContext().isEmpty()) {
                userContent = "【学生画像背景】" + dto.getContext() + "\n\n【问题】" + userContent;
            }
            messages.add(Map.of("role", "user", "content", userContent));
            return deepSeekService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("多模态问答导师调用失败", e);
            throw new RuntimeException("AI导师服务调用失败: " + e.getMessage());
        }
    }
}
