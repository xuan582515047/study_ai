package com.studyai.service;

import com.studyai.dto.PathPlanDTO;
import com.studyai.entity.LearningPath;
import com.studyai.entity.PathStep;
import com.studyai.entity.StudentProfile;
import com.studyai.repository.LearningPathRepository;
import com.studyai.repository.PathStepRepository;
import com.studyai.service.agent.PathPlannerAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository pathRepository;
    private final PathStepRepository stepRepository;
    private final PathPlannerAgent pathPlannerAgent;
    private final StudentProfileService profileService;

    @Transactional
    public LearningPath planPath(PathPlanDTO dto) {
        StudentProfile profile = profileService.getLatestProfile(dto.getStudentId());
        LearningPath path = pathPlannerAgent.planPath(dto, profile);
        pathRepository.save(path);

        List<PathStep> steps = pathPlannerAgent.extractSteps(path.getAiPlanJson(), path.getId());
        for (PathStep step : steps) {
            stepRepository.save(step);
        }
        return path;
    }

    public List<LearningPath> listByStudent(Long studentId) {
        return pathRepository.findByStudentId(studentId);
    }

    public List<PathStep> getPathSteps(Long pathId) {
        return stepRepository.findByPathIdOrderByStepOrderAsc(pathId);
    }
}
