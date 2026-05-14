package com.studyai.controller;

import com.studyai.dto.ProfileBuildDTO;
import com.studyai.dto.Result;
import com.studyai.entity.StudentProfile;
import com.studyai.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService profileService;

    @PostMapping("/build")
    public Result<StudentProfile> buildProfile(@RequestBody ProfileBuildDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setStudentId(userId);
        return Result.success(profileService.buildProfile(dto));
    }

    @GetMapping("/{studentId}")
    public Result<StudentProfile> getProfile(@PathVariable Long studentId) {
        return Result.success(profileService.getLatestProfile(studentId));
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> getProfileStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentProfile profile = profileService.getLatestProfile(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("hasProfile", profile != null);
        return Result.success(result);
    }
}
