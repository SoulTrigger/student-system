package com.student.system;

import com.student.system.util.JwtUtil;
import com.student.system.mapper.StudentMapper;
import com.student.system.mapper.TeacherMapper;
import com.student.system.entity.Student;
import com.student.system.entity.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileUpdateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;

    private String studentToken;
    private String teacherToken;
    private Long studentId;
    private Long teacherId;

    @BeforeEach
    void setUp() {
        String encodedPw = passwordEncoder.encode("oldpass123");

        // Insert test student
        jdbcTemplate.execute("DELETE FROM selection WHERE student_id IN (SELECT id FROM student WHERE name = '__profile_test_stu__')");
        jdbcTemplate.execute("DELETE FROM student WHERE name = '__profile_test_stu__'");
        jdbcTemplate.update("INSERT INTO student (name, password) VALUES ('__profile_test_stu__', ?)", encodedPw);
        studentId = jdbcTemplate.queryForObject("SELECT id FROM student WHERE name = '__profile_test_stu__'", Long.class);
        studentToken = jwtUtil.generateToken(studentId, "student", "__profile_test_stu__");

        // Insert test teacher
        jdbcTemplate.execute("DELETE FROM opening WHERE teacher_id IN (SELECT id FROM teacher WHERE name = '__profile_test_tea__')");
        jdbcTemplate.execute("DELETE FROM teacher WHERE name = '__profile_test_tea__'");
        jdbcTemplate.update("INSERT INTO teacher (name, password) VALUES ('__profile_test_tea__', ?)", encodedPw);
        teacherId = jdbcTemplate.queryForObject("SELECT id FROM teacher WHERE name = '__profile_test_tea__'", Long.class);
        teacherToken = jwtUtil.generateToken(teacherId, "teacher", "__profile_test_tea__");
    }

    @Test
    void testStudentUpdatePassword() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("密码修改成功"));

        Student student = studentMapper.selectById(studentId);
        assertTrue(passwordEncoder.matches("newpass123", student.getPassword()));
    }

    @Test
    void testTeacherUpdatePassword() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("密码修改成功"));

        Teacher teacher = teacherMapper.selectById(teacherId);
        assertTrue(passwordEncoder.matches("newpass123", teacher.getPassword()));
    }

    @Test
    void testPasswordMismatch() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpass123\",\"confirmPassword\":\"different\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("两次密码不一致"));
    }

    @Test
    void testPasswordTooShort() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"12345\",\"confirmPassword\":\"12345\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("密码长度不能少于6位"));
    }

    @Test
    void testNoToken() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminForbidden() throws Exception {
        String adminToken = jwtUtil.generateToken(1L, "admin", "admin");
        mockMvc.perform(put("/api/user/profile")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
            .andExpect(status().isForbidden());
    }
}
