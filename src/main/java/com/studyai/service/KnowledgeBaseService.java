package com.studyai.service;

import com.studyai.dto.Result;
import com.studyai.entity.KnowledgeBase;
import com.studyai.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public Result<List<KnowledgeBase>> listByStudent(Long studentId) {
        List<KnowledgeBase> list = knowledgeBaseRepository.findByStudentIdAndDeleted(studentId, 0);
        return Result.success(list);
    }

    public Result<KnowledgeBase> getDetail(Long id, Long studentId) {
        Optional<KnowledgeBase> optional = knowledgeBaseRepository.findByIdAndStudentIdAndDeleted(id, studentId, 0);
        return optional.map(Result::success)
                .orElseGet(() -> Result.error("知识库不存在"));
    }

    public Result<KnowledgeBase> create(Long studentId, String name, String description, String content,
                                         String fileName, Long fileSize) {
        if (content == null || content.trim().isEmpty()) {
            return Result.error("知识库内容不能为空");
        }
        if (content.length() > 8000) {
            return Result.error("知识库内容不能超过8000字符，请精简内容");
        }
        KnowledgeBase kb = new KnowledgeBase();
        kb.setStudentId(studentId);
        kb.setName(name);
        kb.setDescription(description);
        kb.setContent(content);
        kb.setFileName(fileName);
        kb.setFileSize(fileSize);
        knowledgeBaseRepository.save(kb);
        return Result.success(kb);
    }

    public Result<KnowledgeBase> update(Long id, Long studentId, String name, String description, String content) {
        Optional<KnowledgeBase> optional = knowledgeBaseRepository.findByIdAndStudentIdAndDeleted(id, studentId, 0);
        if (optional.isEmpty()) {
            return Result.error("知识库不存在");
        }
        if (content != null && content.length() > 8000) {
            return Result.error("知识库内容不能超过8000字符，请精简内容");
        }
        KnowledgeBase kb = optional.get();
        if (name != null) kb.setName(name);
        if (description != null) kb.setDescription(description);
        if (content != null) kb.setContent(content);
        knowledgeBaseRepository.save(kb);
        return Result.success(kb);
    }

    public Result<Void> delete(Long id, Long studentId) {
        Optional<KnowledgeBase> optional = knowledgeBaseRepository.findByIdAndStudentIdAndDeleted(id, studentId, 0);
        if (optional.isEmpty()) {
            return Result.error("知识库不存在");
        }
        knowledgeBaseRepository.delete(optional.get());
        return Result.success(null);
    }

    public Optional<KnowledgeBase> findById(Long id) {
        return knowledgeBaseRepository.findByIdAndDeleted(id, 0);
    }
}
