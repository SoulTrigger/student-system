package com.student.service;

import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.mapper.StudentMapper;
import com.student.mapper.TeacherMapper;
import com.student.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired private AuthService authService;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @BeforeEach
    void cleanup() {
        studentMapper.selectList(null).forEach(s -> studentMapper.deleteById(s.getId()));
        teacherMapper.selectList(null).forEach(t -> teacherMapper.deleteById(t.getId()));
    }

    @Test
    void studentLoginReturnsJwt() {
        Student s = new Student();
        s.setName("teststu");
        s.setPassword(passwordEncoder.encode("123456"));
        studentMapper.insert(s);

        Map<String, Object> result = authService.login(String.valueOf(s.getId()), "123456", "student");
        assertNotNull(result.get("token"));
        assertEquals("student", result.get("role"));
        assertEquals(s.getId(), result.get("userId"));
        assertTrue(jwtUtil.isTokenValid((String) result.get("token")));
    }

    @Test
    void teacherLoginReturnsJwt() {
        Teacher t = new Teacher();
        t.setName("testtch");
        t.setPassword(passwordEncoder.encode("123456"));
        teacherMapper.insert(t);

        Map<String, Object> result = authService.login(String.valueOf(t.getId()), "123456", "teacher");
        assertNotNull(result.get("token"));
        assertEquals("teacher", result.get("role"));
    }

    @Test
    void wrongPasswordReturnsError() {
        Student s = new Student();
        s.setName("teststu");
        s.setPassword(passwordEncoder.encode("123456"));
        studentMapper.insert(s);

        assertThrows(RuntimeException.class, () ->
                authService.login(String.valueOf(s.getId()), "wrong", "student"));
    }

    @Test
    void nonexistentUserReturnsError() {
        assertThrows(RuntimeException.class, () ->
                authService.login("99999", "123456", "student"));
    }

    @Test
    void changePasswordSuccess() {
        Student s = new Student();
        s.setName("teststu");
        s.setPassword(passwordEncoder.encode("123456"));
        studentMapper.insert(s);

        authService.changePassword(s.getId(), "student", "123456", "654321");

        Student updated = studentMapper.selectById(s.getId());
        assertTrue(passwordEncoder.matches("654321", updated.getPassword()));
    }

    @Test
    void changePasswordTooShortFails() {
        Student s = new Student();
        s.setName("teststu");
        s.setPassword(passwordEncoder.encode("123456"));
        studentMapper.insert(s);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(s.getId(), "student", "123456", "12"));
    }

    @Test
    void changePasswordWrongOldFails() {
        Student s = new Student();
        s.setName("teststu");
        s.setPassword(passwordEncoder.encode("123456"));
        studentMapper.insert(s);

        assertThrows(RuntimeException.class, () ->
                authService.changePassword(s.getId(), "student", "wrong", "654321"));
    }

    @Test
    void jwtContainsCorrectClaims() {
        Teacher t = new Teacher();
        t.setName("testtch");
        t.setPassword(passwordEncoder.encode("123456"));
        teacherMapper.insert(t);

        Map<String, Object> result = authService.login(String.valueOf(t.getId()), "123456", "teacher");
        String token = (String) result.get("token");

        assertEquals(t.getId(), jwtUtil.getUserId(token));
        assertEquals("teacher", jwtUtil.getRole(token));
    }
}
