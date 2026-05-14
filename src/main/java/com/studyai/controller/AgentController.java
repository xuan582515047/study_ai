package com.studyai.controller;

import com.studyai.dto.AgentRequestDTO;
import com.studyai.dto.Result;
import com.studyai.entity.KnowledgeBase;
import com.studyai.service.KnowledgeBaseService;
import com.studyai.service.agent.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.sl.usermodel.TextParagraph;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final MultiModalTutorAgent multiModalTutorAgent;
    private final PPTGeneratorAgent pptGeneratorAgent;
    private final DocGeneratorAgent docGeneratorAgent;
    private final QuizGeneratorAgent quizGeneratorAgent;
    private final MindMapGeneratorAgent mindMapGeneratorAgent;
    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/{agentType}")
    public Result<String> invokeAgent(@PathVariable String agentType,
                                      @RequestBody AgentRequestDTO dto,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (dto.getStudentId() == null) {
            dto.setStudentId(userId);
        }

        // 注入知识库约束到 context
        injectKnowledgeBase(dto);

        String result;
        switch (agentType) {
            case "tutor":
                result = multiModalTutorAgent.tutor(dto);
                break;
            case "ppt":
                result = pptGeneratorAgent.generatePPT(dto);
                break;
            case "doc":
                result = docGeneratorAgent.generateDoc(dto);
                break;
            case "quiz":
                result = quizGeneratorAgent.generateQuiz(dto);
                break;
            case "mindmap":
                result = mindMapGeneratorAgent.generateMindMap(dto);
                break;
            default:
                return Result.error("未知的智能体类型: " + agentType);
        }
        return Result.success(result);
    }

    /**
     * PPTX 文件下载接口
     * 将 AI 生成的 Markdown 内容转换为 PPTX 文件并返回
     */
    @PostMapping("/ppt/download")
    public ResponseEntity<Resource> downloadPPT(@RequestBody Map<String, String> body) throws Exception {
        String content = body.get("content");
        String title = body.getOrDefault("title", "AI Presentation");

        if (content == null || content.isEmpty()) {
            content = "无内容";
        }

        // 解析 Markdown 内容，提取标题和正文
        List<Map<String, Object>> slides = parseMarkdownToSlides(content);
        if (slides.isEmpty()) {
            Map<String, Object> defaultSlide = new HashMap<>();
            defaultSlide.put("title", title);
            defaultSlide.put("content", content);
            slides.add(defaultSlide);
        }

        // 生成 PPTX
        File tempFile = File.createTempFile("ai_ppt_", ".pptx");
        tempFile.deleteOnExit();

        try (XMLSlideShow ppt = new XMLSlideShow()) {
            // 设置页面大小 (16:9)
            ppt.setPageSize(new java.awt.Dimension(960, 540));

            for (Map<String, Object> slideData : slides) {
                XSLFSlide slide = ppt.createSlide();

                // 标题
                String slideTitle = (String) slideData.getOrDefault("title", "");
                XSLFTextShape titleShape = slide.createTextBox();
                titleShape.setShapeType(org.apache.poi.sl.usermodel.ShapeType.RECT);
                titleShape.setAnchor(new java.awt.Rectangle(60, 30, 840, 60));
                titleShape.setText(slideTitle);
                for (XSLFTextParagraph p : titleShape.getTextParagraphs()) {
                    p.setTextAlign(TextParagraph.TextAlign.LEFT);
                    for (XSLFTextRun run : p.getTextRuns()) {
                        run.setFontSize(28.0);
                        run.setBold(true);
                        run.setFontColor(java.awt.Color.decode("#1a1a2e"));
                    }
                }

                // 内容
                String slideContent = (String) slideData.getOrDefault("content", "");
                XSLFTextShape contentShape = slide.createTextBox();
                contentShape.setAnchor(new java.awt.Rectangle(60, 110, 840, 380));
                contentShape.setText(slideContent);
                for (XSLFTextParagraph p : contentShape.getTextParagraphs()) {
                    p.setTextAlign(TextParagraph.TextAlign.LEFT);
                    p.setLineSpacing(24.0);
                    for (XSLFTextRun run : p.getTextRuns()) {
                        run.setFontSize(16.0);
                        run.setFontColor(java.awt.Color.decode("#333333"));
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ppt.write(fos);
            }
        }

        // 返回文件流
        InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));
        String encodedFileName = URLEncoder.encode(title + ".pptx", StandardCharsets.UTF_8)
            .replace("+", "%20");

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
            .body(resource);
    }

    /**
     * 解析 Markdown 内容为幻灯片数据
     * 标题（# ## ###）作为幻灯片标题，后续内容作为幻灯片正文
     */
    private List<Map<String, Object>> parseMarkdownToSlides(String markdown) {
        List<Map<String, Object>> slides = new ArrayList<>();
        String[] lines = markdown.split("\n");

        StringBuilder currentContent = new StringBuilder();
        String currentTitle = "";

        // 正则匹配标题行
        Pattern headingPattern = Pattern.compile("^(#{1,3})\\s+(.+)$");

        for (String line : lines) {
            Matcher matcher = headingPattern.matcher(line);
            if (matcher.matches()) {
                // 保存上一张幻灯片
                if (!currentTitle.isEmpty() || currentContent.length() > 0) {
                    Map<String, Object> slide = new HashMap<>();
                    slide.put("title", currentTitle);
                    slide.put("content", currentContent.toString().trim());
                    slides.add(slide);
                }

                currentTitle = matcher.group(2).trim();
                currentContent = new StringBuilder();
            } else {
                if (!line.trim().isEmpty() || currentContent.length() > 0) {
                    // 过滤 Markdown 标记（粗体、斜体、行内代码等）
                    String cleanLine = line.replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                                            .replaceAll("\\*(.+?)\\*", "$1")
                                            .replaceAll("`(.+?)`", "$1")
                                            .replaceAll("~~(.+?)~~", "$1");
                    currentContent.append(cleanLine).append("\n");
                }
            }
        }

        // 添加最后一张幻灯片
        if (!currentTitle.isEmpty() || currentContent.length() > 0) {
            Map<String, Object> slide = new HashMap<>();
            slide.put("title", currentTitle);
            slide.put("content", currentContent.toString().trim());
            slides.add(slide);
        }

        return slides;
    }

    private void injectKnowledgeBase(AgentRequestDTO dto) {
        Long kbId = dto.getKnowledgeBaseId();
        if (kbId == null || kbId <= 0) {
            return;
        }
        Optional<KnowledgeBase> kbOpt = knowledgeBaseService.findById(kbId);
        if (kbOpt.isEmpty()) {
            return;
        }
        KnowledgeBase kb = kbOpt.get();
        String kbContent = kb.getContent();
        if (kbContent.length() > 8000) {
            kbContent = kbContent.substring(0, 8000) + "\n...（知识库内容过长，已截断）";
        }

        String constraint = String.format(
            "【知识库约束 - 严格遵守】\n" +
            "以下是用户提供的知识库资料，你必须严格基于这些资料来回答问题。\n" +
            "- 你的回答内容必须来源于以下知识库\n" +
            "- 如果知识库中没有相关信息，请明确告知用户\"该问题超出了当前知识库的范围\"\n" +
            "- 不得编造或使用知识库之外的知识来回答\n\n" +
            "【知识库名称：%s】\n%s\n\n【知识库结束】",
            kb.getName(),
            kbContent
        );

        String existingContext = dto.getContext();
        if (existingContext != null && !existingContext.isEmpty()) {
            dto.setContext(constraint + "\n\n" + existingContext);
        } else {
            dto.setContext(constraint);
        }
    }
}
