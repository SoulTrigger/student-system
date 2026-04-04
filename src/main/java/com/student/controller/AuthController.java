package com.student.controller;

import com.student.common.Result;
import com.student.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String password = body.get("password");
        String role = body.get("role");
        try {
            return Result.success(authService.login(id, password, role));
        } catch (RuntimeException e) {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body,
                                       @RequestAttribute("userId") Long userId,
                                       @RequestAttribute("role") String role) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        try {
            authService.changePassword(userId, role, oldPassword, newPassword);
            return Result.success();
        } catch (RuntimeException e) {
            if ("PASSWORD_TOO_SHORT".equals(e.getMessage())) {
                return Result.error(400, "新密码长度不能少于6位");
            }
            return Result.error(401, "旧密码错误");
        }
    }
}
