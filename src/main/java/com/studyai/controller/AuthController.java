package com.studyai.controller;

import com.studyai.dto.LoginDTO;
import com.studyai.dto.RegisterDTO;
import com.studyai.dto.Result;
import com.studyai.dto.UpdateProfileDTO;
import com.studyai.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> data = authService.login(loginDTO);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(authService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<Map<String, Object>> updateProfile(@RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(authService.updateProfile(userId, dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
