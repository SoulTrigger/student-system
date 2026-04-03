package com.student.system.controller;

import com.student.system.dto.LoginRequest;
import com.student.system.dto.LoginResponse;
import com.student.system.entity.Student;
import com.student.system.entity.Teacher;
import com.student.system.mapper.StudentMapper;
import com.student.system.mapper.TeacherMapper;
import com.student.system.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Long uid;
        String name = null;
        String storedPassword = null;

        try {
            uid = Long.parseLong(request.getUserId());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody("用户名或密码错误"));
        }

        switch (request.getRole()) {
            case "student": {
                Student student = studentMapper.selectById(uid);
                if (student != null) {
                    name = student.getName();
                    storedPassword = student.getPassword();
                }
                break;
            }
            case "teacher":
            case "admin": {
                Teacher teacher = teacherMapper.selectById(uid);
                if (teacher != null) {
                    name = teacher.getName();
                    storedPassword = teacher.getPassword();
                }
                break;
            }
            default:
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorBody("用户名或密码错误"));
        }

        if (storedPassword == null || !passwordEncoder.matches(request.getPassword(), storedPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody("用户名或密码错误"));
        }

        String role = "admin".equals(request.getRole()) ? "admin" : request.getRole();
        String token = jwtUtil.generateToken(uid, role, name);

        return ResponseEntity.ok(new LoginResponse(token, role, name, uid));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> body = new HashMap<>();
        body.put("message", "退出成功");
        return ResponseEntity.ok(body);
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
