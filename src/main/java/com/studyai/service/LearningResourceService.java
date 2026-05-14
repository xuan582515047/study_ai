package com.studyai.service;

import com.studyai.dto.ResourceGenDTO;
import com.studyai.entity.LearningResource;
import com.studyai.entity.StudentProfile;
import com.studyai.repository.LearningResourceRepository;
import com.studyai.service.agent.ResourceGeneratorAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private final LearningResourceRepository resourceRepository;
    private final ResourceGeneratorAgent resourceGeneratorAgent;
    private final StudentProfileService profileService;

    @Transactional
    public List<LearningResource> generateResources(ResourceGenDTO dto) {
        StudentProfile profile = profileService.getLatestProfile(dto.getStudentId());
        List<LearningResource> resources = resourceGeneratorAgent.generateResources(dto, profile);
        for (LearningResource resource : resources) {
            resourceRepository.save(resource);
        }
        return resources;
    }

    public List<LearningResource> listByStudent(Long studentId) {
        return resourceRepository.findByStudentId(studentId);
    }
}
