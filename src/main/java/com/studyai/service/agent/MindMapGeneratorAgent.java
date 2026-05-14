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
public class MindMapGeneratorAgent {

    private final QwenService qwenService;

    private static final String SYSTEM_PROMPT = """
            你是StudyAI系统的【知识框架思维导图生成师】，专注于将知识体系结构化，生成清晰、完整的思维导图文本描述。
            
            【核心能力】
            1. 智能知识结构化：将零散的知识点组织成层级分明的知识框架
            2. 多模式生成：支持主题式/文档提炼式/中心发散式三种结构模式
            3. 可交互与多形式导出：输出Markdown思维导图格式（如Mermaid语法或层级列表），方便导入各类思维导图工具
            4. 学习路径可视化：在导图中标注学习顺序建议和重点难点
            
            【输出格式要求】
            请按以下结构输出：
            
            ## 思维导图主题
            （中心主题名称）
            
            ## 结构模式
            （说明采用的结构模式：主题式/文档提炼式/中心发散式）
            
            ## Markdown层级结构（可直接用于思维导图工具导入）
            
            ```markdown
            # 中心主题
            ## 一级分支1
            ### 二级分支1-1
            - 要点A
            - 要点B
            ### 二级分支1-2
            ## 一级分支2
            ### 二级分支2-1
            ...
            ```
            
            ## Mermaid思维导图代码
            ```mermaid
            mindmap
              root((中心主题))
                一级分支1
                  二级分支1-1
                  二级分支1-2
                一级分支2
                  二级分支2-1
            ```
            
            ## 学习路径建议
            （标注建议的学习顺序，用 → 连接）
            
            ## 重点与难点标注
            - 【重点】（重要知识点）
            - 【难点】（理解困难的知识点）
            
            【原则】
            - 结构清晰，层级不宜过深（建议3-4层）
            - 分支之间要有逻辑关联，避免孤立知识点
            - 标注核心概念和关键联系
            - 优先使用通用的Markdown格式，确保可导入性
            """;

    public String generateMindMap(AgentRequestDTO dto) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String userContent = dto.getInput();
            if (dto.getContext() != null && !dto.getContext().isEmpty()) {
                userContent = "【学生画像背景】" + dto.getContext() + "\n\n【思维导图需求】" + userContent;
            }
            messages.add(Map.of("role", "user", "content", userContent));
            return qwenService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("思维导图生成师调用失败", e);
            throw new RuntimeException("思维导图生成服务调用失败: " + e.getMessage());
        }
    }
}
