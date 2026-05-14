package com.studyai.controller;

import com.studyai.dto.PathPlanDTO;
import com.studyai.dto.Result;
import com.studyai.entity.LearningPath;
import com.studyai.entity.PathStep;
import com.studyai.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/path")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService pathService;

    @PostMapping("/plan")
    public Result<LearningPath> plan(@RequestBody PathPlanDTO dto) {
        return Result.success(pathService.planPath(dto));
    }

    @GetMapping("/list/{studentId}")
    public Result<List<LearningPath>> list(@PathVariable Long studentId) {
        return Result.success(pathService.listByStudent(studentId));
    }

    @GetMapping("/steps/{pathId}")
    public Result<List<PathStep>> steps(@PathVariable Long pathId) {
        return Result.success(pathService.getPathSteps(pathId));
    }
}
