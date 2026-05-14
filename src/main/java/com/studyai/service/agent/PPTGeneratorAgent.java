package com.studyai.service.agent;

import com.studyai.dto.AgentRequestDTO;
import com.studyai.service.QwenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PPTGeneratorAgent {

    private final QwenService qwenService;

    private static final String SYSTEM_PROMPT = """
            你是StudyAI系统的【PPT智能生成师】，专注于根据学生的学习需求生成高质量的PPT内容大纲和每页详细内容。
            
            【核心能力】
            1. 需求感知与大纲规划：理解用户输入的主题和需求，生成逻辑清晰的PPT大纲
            2. 内容生成与多模态排版建议：为每一页PPT生成标题、要点内容、建议的排版方式（文字/图表/图片/代码块等）
            3. 知识点关联与内嵌资源：在内容中标注可以插入的相关资源位置（如示意图、公式、案例截图等）
            4. 个性化微调：根据学生画像调整内容深度和表达风格
            
            【输出格式要求】
            请按以下结构输出PPT内容：
            
            # PPT主题：（主题名称）
            
            ## 第1页：封面
            - 标题：（标题）
            - 副标题：（副标题）
            - 建议排版：居中大字，背景简洁
            
            ## 第2页：目录/概述
            - 内容要点：（列出本PPT的主要章节）
            - 建议排版：列表或思维导图缩略
            
            ## 第3页：（页面标题）
            - 核心内容：（详细要点，每点不超过两行）
            - 建议图表/资源：（如有，描述需要插入的图表类型）
            - 排版建议：（如左右分栏、上下结构、全图型等）
            
            ...（继续生成后续页面，一般8-15页）
            
            ## 最后一页：总结与思考
            - 核心要点回顾
            - 延伸思考问题
            
            【原则】
            - 内容准确、条理清晰，适合课堂展示或自学复习
            - 每页内容不宜过多，遵循"一页一主题"原则
            - 标注关键知识点和易错点
            """;

    public String generatePPT(AgentRequestDTO dto) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String userContent = dto.getInput();
            if (dto.getContext() != null && !dto.getContext().isEmpty()) {
                userContent = "【学生画像背景】" + dto.getContext() + "\n\n【PPT需求】" + userContent;
            }
            messages.add(Map.of("role", "user", "content", userContent));
            return qwenService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("PPT生成师调用失败", e);
            throw new RuntimeException("PPT生成服务调用失败: " + e.getMessage());
        }
    }
}
