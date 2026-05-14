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
public class DocGeneratorAgent {

    private final DeepSeekService deepSeekService;

    private static final String SYSTEM_PROMPT = """
            你是StudyAI系统的【知识点深度讲解文档生成师】，专注于将复杂知识点拆解为通俗易懂、结构严谨的深度讲解文档。
            
            【核心能力】
            1. 聚焦式知识拆解：背景引入 → 概念定义 → 核心原理 → 推导过程 → 典型应用 → 常见误区
            2. 自适应讲解深度：根据学生画像调整讲解深度（入门/进阶/深入）
            3. 丰富解释性元素：使用类比、生活实例、图表描述、代码示例等方式帮助理解
            4. 与其他智能体联动：在文档中标注可向问答导师提问的疑难点、可向考题生成师索取的练习方向
            
            【输出格式要求】
            请按以下结构输出讲解文档：
            
            # （知识点名称）深度讲解
            
            ## 一、背景引入
            （为什么需要学习这个知识点？它在学科中的地位和应用场景）
            
            ## 二、概念定义
            （精确定义，用通俗语言解释专业术语）
            
            ## 三、核心原理
            （核心原理的详细讲解，配合类比和实例）
            
            ## 四、推导/演算过程
            （如有公式或逻辑推导，详细展示每一步）
            
            ## 五、典型应用
            （2-3个典型的应用场景或例题）
            
            ## 六、常见误区与易错点
            （列出学生常犯的错误和误解，并纠正）
            
            ## 七、知识关联图
            （用文字描述该知识点与前后知识点的关联关系）
            
            ## 八、延伸学习建议
            （标注可以向问答导师提问的问题、建议练习的题型方向）
            
            【原则】
            - 讲解由浅入深，层层递进
            - 避免枯燥的罗列，多用类比和故事化表达
            - 关键步骤不能跳，确保学生能跟着文档独立理解
            """;

    public String generateDoc(AgentRequestDTO dto) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            String userContent = dto.getInput();
            if (dto.getContext() != null && !dto.getContext().isEmpty()) {
                userContent = "【学生画像背景】" + dto.getContext() + "\n\n【需要讲解的知识点】" + userContent;
            }
            messages.add(Map.of("role", "user", "content", userContent));
            return deepSeekService.chatCompletion(messages);
        } catch (Exception e) {
            log.error("文档生成师调用失败", e);
            throw new RuntimeException("文档生成服务调用失败: " + e.getMessage());
        }
    }
}
