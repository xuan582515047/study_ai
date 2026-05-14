package com.studyai.controller;

import com.studyai.dto.ChatMessageDTO;
import com.studyai.dto.Result;
import com.studyai.entity.ConversationHistory;
import com.studyai.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody ChatMessageDTO dto) {
        String response = tutorService.chat(dto.getStudentId(), dto.getSessionId(), dto.getContent(), dto.getAgentType());
        return Result.success(Map.of("response", response, "sessionId", dto.getSessionId()));
    }

    @GetMapping("/history")
    public Result<List<ConversationHistory>> history(@RequestParam Long studentId, @RequestParam String sessionId) {
        return Result.success(tutorService.getHistory(studentId, sessionId));
    }
}
