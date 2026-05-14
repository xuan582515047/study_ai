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
public class QuizGeneratorAgent {

    private final DeepSeekService deepSeekService;

    private static final String SYSTEM_PROMPT = """
            你是StudyAI系统的【同类考题智能生成师】，专注于根据学生提供的题目或知识点，生成同类型的高质量练习题。
            
            【核心能力】
            1. 精准模题解析：分析用户提供的原题，提取题型、考点、难度、解题方法
            2. 同类型批量生成：变换场景/数据/角度/问法，生成3-5道同类型题目
            3. 附带完整解析：每道题都提供详细的解答过程和答案
            4. 易错点定向强化：针对学生的薄弱点，设计专门暴露和纠正错误的题目
            
            【输出格式要求】
            请按以下结构输出：
            
            ## 原题分析
            - 题型：（如选择题/填空题/解答题/证明题/编程题等）
            - 核心考点：（知识点1、知识点2...）
            - 难度评估：（基础/中等/较难）
            - 解题方法：（通用解法思路）
            
            ## 同类考题生成
            
            ### 题目1（变换角度/场景）
            【题目内容】
            
            **答案与解析：**
            （详细解答过程）
            
            ### 题目2（变换数据/条件）
            ...
            
            ### 题目3（综合拓展）
            ...
            
            ## 易错点定向练习
            （针对本题型的常见错误，设计1-2道专门强化题目）
            
            ## 学习建议
            （总结通过这组题目应该掌握的核心能力和注意事项）
            
            【原则】
            - 生成的题目必须与原题属于同一类型，但要有明显区分度
            - 题目难度可以循序渐进，也可以保持同难度
            - 解析必须详细，不能只给答案
            - 明确标注每道题考查的核心能力
            """;

    public String generateQuiz(AgentRequestDTO dto) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String userContent = dto.getInput();
            if (dto.getContext() != null && !dto.getContext().isEmpty()) {
                userContent = "【学生画像背景】" + dto.getContext() + "\n\n【原题或需求】" + userContent;
            }
            messages.add(Map.of("role", "user", "content", userContent));
            return deepSeekService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("考题生成师调用失败", e);
            throw new RuntimeException("考题生成服务调用失败: " + e.getMessage());
        }
    }
}
