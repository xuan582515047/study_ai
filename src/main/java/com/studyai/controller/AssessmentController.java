package com.studyai.controller;

import com.studyai.dto.AssessmentDTO;
import com.studyai.dto.Result;
import com.studyai.entity.AssessmentResult;
import com.studyai.entity.QuizQuestion;
import com.studyai.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping("/questions/generate")
    public Result<List<QuizQuestion>> generateQuestions(
            @RequestParam String subject,
            @RequestParam String knowledgePoint,
            @RequestParam(defaultValue = "medium") String difficulty,
            @RequestParam(defaultValue = "5") int count) {
        return Result.success(assessmentService.generateQuestions(subject, knowledgePoint, difficulty, count));
    }

    @PostMapping("/submit")
    public Result<AssessmentResult> submit(@RequestBody AssessmentDTO dto) {
        return Result.success(assessmentService.submitAssessment(dto));
    }

    @GetMapping("/list/{studentId}")
    public Result<List<AssessmentResult>> list(@PathVariable Long studentId) {
        return Result.success(assessmentService.listByStudent(studentId));
    }
}
