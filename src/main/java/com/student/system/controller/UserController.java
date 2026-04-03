package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.dto.ProfileUpdateRequest;
import com.student.system.entity.Student;
import com.student.system.entity.Teacher;
import com.student.system.mapper.StudentMapper;
import com.student.system.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Validated @RequestBody ProfileUpdateRequest request) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "两次密码不一致"));
            }
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "密码长度不能少于6位"));
            }

            Long userId = RequestContextHolder.getUserId();
            String role = RequestContextHolder.getRole();
            String encoded = passwordEncoder.encode(request.getNewPassword());

            if ("student".equals(role)) {
                Student student = studentMapper.selectById(userId);
                if (student == null) {
                    return ResponseEntity.status(404).body(Map.of("error", "用户不存在"));
                }
                student.setPassword(encoded);
                studentMapper.updateById(student);
            } else if ("teacher".equals(role)) {
                Teacher teacher = teacherMapper.selectById(userId);
                if (teacher == null) {
                    return ResponseEntity.status(404).body(Map.of("error", "用户不存在"));
                }
                teacher.setPassword(encoded);
                teacherMapper.updateById(teacher);
            } else {
                return ResponseEntity.status(403).body(Map.of("error", "无权操作"));
            }

            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
