package com.studyai.controller;

import com.studyai.dto.Result;
import com.studyai.entity.ConversationHistory;
import com.studyai.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取某个智能体的会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ConversationService.SessionSummary>> listSessions(
            @RequestParam String agentType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        return Result.success(conversationService.listSessions(userId, agentType));
    }

    /**
     * 获取某个会话的消息列表
     */
    @GetMapping("/messages")
    public Result<List<ConversationHistory>> getMessages(
            @RequestParam String sessionId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        return Result.success(conversationService.getMessages(userId, sessionId));
    }

    /**
     * 保存一条消息
     */
    @PostMapping("/message")
    public Result<ConversationHistory> saveMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未登录");

        String sessionId = (String) body.get("sessionId");
        String role = (String) body.get("role");
        String content = (String) body.get("content");
        String agentType = (String) body.get("agentType");

        if (sessionId == null || sessionId.isEmpty()) {
            return Result.error("sessionId 不能为空");
        }
        if (role == null || content == null || agentType == null) {
            return Result.error("参数不完整");
        }

        return Result.success(conversationService.saveMessage(userId, sessionId, role, content, agentType));
    }

    /**
     * 删除某个会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        conversationService.deleteSession(userId, sessionId);
        return Result.success();
    }
}
