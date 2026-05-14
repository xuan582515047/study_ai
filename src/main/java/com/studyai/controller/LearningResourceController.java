package com.studyai.controller;

import com.studyai.dto.ResourceGenDTO;
import com.studyai.dto.Result;
import com.studyai.entity.LearningResource;
import com.studyai.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService resourceService;

    @PostMapping("/generate")
    public Result<List<LearningResource>> generate(@RequestBody ResourceGenDTO dto) {
        return Result.success(resourceService.generateResources(dto));
    }

    @GetMapping("/list/{studentId}")
    public Result<List<LearningResource>> list(@PathVariable Long studentId) {
        return Result.success(resourceService.listByStudent(studentId));
    }
}
