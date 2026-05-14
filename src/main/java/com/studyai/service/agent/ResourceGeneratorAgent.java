package com.studyai.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyai.dto.ResourceGenDTO;
import com.studyai.entity.LearningResource;
import com.studyai.entity.StudentProfile;
import com.studyai.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceGeneratorAgent {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<LearningResource> generateResources(ResourceGenDTO dto, StudentProfile profile) {
        List<String> types = dto.getResourceTypes();
        if (types == null || types.isEmpty()) {
            types = List.of("document", "mindmap", "exercise", "codecase", "reading");
        }

        List<CompletableFuture<LearningResource>> futures = new ArrayList<>();
        for (String type : types) {
            futures.add(CompletableFuture.supplyAsync(() -> generateSingleResource(type, dto, profile)));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private LearningResource generateSingleResource(String type, ResourceGenDTO dto, StudentProfile profile) {
        String systemPrompt = getSystemPromptForType(type);
        String userPrompt = buildUserPrompt(dto, profile);

        try {
            String content = deepSeekService.chatWithSystemPrompt(systemPrompt, userPrompt);
            LearningResource resource = new LearningResource();
            resource.setStudentId(dto.getStudentId());
            resource.setResourceType(type);
            resource.setTitle(extractTitle(content, type, dto.getTopic()));
            resource.setContent(content);
            resource.setSubject(dto.getSubject());
            resource.setDifficultyLevel(dto.getDifficulty());
            resource.setKnowledgePoints(dto.getTopic());
            resource.setStatus("generated");
            return resource;
        } catch (Exception e) {
            log.error("资源生成失败, type={}", type, e);
            LearningResource fallback = new LearningResource();
            fallback.setStudentId(dto.getStudentId());
            fallback.setResourceType(type);
            fallback.setTitle(dto.getTopic() + " - " + type);
            fallback.setContent("生成失败，请重试");
            fallback.setSubject(dto.getSubject());
            return fallback;
        }
    }

    private String getSystemPromptForType(String type) {
        return switch (type) {
            case "document" -> "你是一位资深教育专家，请根据学生画像和主题生成一份结构化的学习文档，包含概念解释、原理说明、示例分析。用Markdown格式输出。";
            case "mindmap" -> "你是一位知识图谱专家，请根据主题生成思维导图内容，使用Markdown列表层级结构表示节点关系。";
            case "exercise" -> "你是一位出题专家，请根据主题生成5道练习题（包含单选、多选、填空、简答），每题附带答案和解析。用Markdown格式输出。";
            case "codecase" -> "你是一位编程导师，请根据主题生成可运行的代码案例，包含详细注释和运行说明。用Markdown代码块格式输出。";
            case "reading" -> "你是一位学术推荐专家，请根据主题推荐相关的拓展阅读材料，包含书名/论文名、作者、核心观点摘要。用Markdown格式输出。";
            case "video" -> "你是一位课程设计师，请根据主题设计一段视频/动画的教学脚本大纲，包含场景描述、旁白、画面说明。用Markdown格式输出。";
            default -> "你是一位教育专家，请根据主题生成学习资源内容。";
        };
    }

    private String buildUserPrompt(ResourceGenDTO dto, StudentProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("主题：").append(dto.getTopic()).append("\n");
        sb.append("学科：").append(dto.getSubject()).append("\n");
        sb.append("难度：").append(dto.getDifficulty()).append("\n");
        if (profile != null) {
            sb.append("学生画像：\n");
            sb.append("- 知识基础：").append(profile.getKnowledgeBase()).append("\n");
            sb.append("- 认知风格：").append(profile.getCognitiveStyle()).append("\n");
            sb.append("- 薄弱点：").append(profile.getWeakPoints()).append("\n");
            sb.append("- 资源偏好：").append(profile.getResourcePreference()).append("\n");
        }
        return sb.toString();
    }

    private String extractTitle(String content, String type, String topic) {
        if (content == null || content.isEmpty()) return topic + " - " + type;
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("# ")) return line.substring(2).trim();
            if (line.startsWith("## ")) return line.substring(3).trim();
        }
        return topic + " - " + type;
    }
}
