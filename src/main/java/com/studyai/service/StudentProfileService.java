package com.studyai.service;

import com.studyai.dto.ProfileBuildDTO;
import com.studyai.entity.StudentProfile;
import com.studyai.repository.StudentProfileRepository;
import com.studyai.service.agent.ProfileBuilderAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final ProfileBuilderAgent profileBuilderAgent;

    @Transactional
    public StudentProfile buildProfile(ProfileBuildDTO dto) {
        StudentProfile profile = profileBuilderAgent.buildProfile(dto);
        profileRepository.save(profile);
        return profile;
    }

    public StudentProfile getLatestProfile(Long studentId) {
        return profileRepository.findTopByStudentIdOrderByVersionDesc(studentId);
    }
}
