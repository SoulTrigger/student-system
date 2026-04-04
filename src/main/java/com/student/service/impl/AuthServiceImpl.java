package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.mapper.StudentMapper;
import com.student.mapper.TeacherMapper;
import com.student.service.AuthService;
import com.student.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String idStr, String password, String role) {
        Long id;
        try {
            id = Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        String encodedPassword;
        switch (role.toLowerCase()) {
            case "student" -> {
                Student s = studentMapper.selectById(id);
                if (s == null) throw new RuntimeException("INVALID_CREDENTIALS");
                encodedPassword = s.getPassword();
            }
            case "teacher", "admin" -> {
                Teacher t = teacherMapper.selectById(id);
                if (t == null) throw new RuntimeException("INVALID_CREDENTIALS");
                encodedPassword = t.getPassword();
            }
            default -> throw new RuntimeException("INVALID_CREDENTIALS");
        }

        if (!passwordEncoder.matches(password, encodedPassword)) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        String token = jwtUtil.generateToken(id, role.toLowerCase());
        return Map.of("token", token, "role", role.toLowerCase(), "userId", id);
    }

    @Override
    public void changePassword(Long userId, String role, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("PASSWORD_TOO_SHORT");
        }

        switch (role.toLowerCase()) {
            case "student" -> {
                Student s = studentMapper.selectById(userId);
                if (s == null || !passwordEncoder.matches(oldPassword, s.getPassword())) {
                    throw new RuntimeException("INVALID_CREDENTIALS");
                }
                s.setPassword(passwordEncoder.encode(newPassword));
                studentMapper.updateById(s);
            }
            case "teacher", "admin" -> {
                Teacher t = teacherMapper.selectById(userId);
                if (t == null || !passwordEncoder.matches(oldPassword, t.getPassword())) {
                    throw new RuntimeException("INVALID_CREDENTIALS");
                }
                t.setPassword(passwordEncoder.encode(newPassword));
                teacherMapper.updateById(t);
            }
            default -> throw new RuntimeException("INVALID_ROLE");
        }
    }
}
