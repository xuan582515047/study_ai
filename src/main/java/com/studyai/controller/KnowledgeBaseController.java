package com.studyai.controller;

import com.studyai.dto.Result;
import com.studyai.entity.KnowledgeBase;
import com.studyai.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/list")
    public Result<List<KnowledgeBase>> list(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return knowledgeBaseService.listByStudent(studentId);
    }

    @GetMapping("/{id}")
    public Result<KnowledgeBase> detail(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return knowledgeBaseService.getDetail(id, studentId);
    }

    @PostMapping("/create")
    public Result<KnowledgeBase> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String content = (String) body.get("content");
        String fileName = (String) body.get("fileName");
        Number fileSizeNum = (Number) body.get("fileSize");
        Long fileSize = fileSizeNum != null ? fileSizeNum.longValue() : null;

        if (name == null || name.trim().isEmpty()) {
            return Result.error("知识库名称不能为空");
        }
        return knowledgeBaseService.create(studentId, name, description, content, fileName, fileSize);
    }

    @PutMapping("/update/{id}")
    public Result<KnowledgeBase> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String content = (String) body.get("content");
        return knowledgeBaseService.update(id, studentId, name, description, content);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return knowledgeBaseService.delete(id, studentId);
    }
}
