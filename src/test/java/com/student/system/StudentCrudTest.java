package com.student.system;

import com.student.system.entity.Student;
import com.student.system.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentCrudTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private StudentMapper studentMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("DELETE FROM grade");
        jdbc.execute("DELETE FROM selection");
        jdbc.execute("DELETE FROM student");
        jdbc.execute("DELETE FROM opening");
        jdbc.execute("DELETE FROM course");
        jdbc.execute("DELETE FROM teacher WHERE name != 'admin'");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"userId\":\"1\",\"password\":\"admin123\",\"role\":\"admin\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        adminToken = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(body).get("token").asText();
    }

    @Test
    void listStudentsPaginated_noPassword() throws Exception {
        jdbc.execute("INSERT INTO student (name, password) VALUES ('测试学生', '$2a$10$dummy')");

        mockMvc.perform(get("/api/admin/students?page=1&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].name").value("测试学生"))
                .andExpect(jsonPath("$.records[0].password").doesNotExist());
    }

    @Test
    void createStudent_withDefaultPassword() throws Exception {
        mockMvc.perform(post("/api/admin/students")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"张三\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        Long studentId = studentMapper.selectList(null).get(0).getId();
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"userId\":\"" + studentId + "\",\"password\":\"123456\",\"role\":\"student\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStudent() throws Exception {
        jdbc.execute("INSERT INTO student (name, password) VALUES ('李四', '$2a$10$dummy')");
        Long id = studentMapper.selectList(null).get(0).getId();

        mockMvc.perform(put("/api/admin/students/" + id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"李五\",\"password\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("李五"));
    }

    @Test
    void deleteStudent_cascadeDeletesSelectionsAndGrades() throws Exception {
        // Insert student, course, opening, selection, grade with proper FK chain
        jdbc.execute("INSERT INTO student (name, password) VALUES ('王五', '$2a$10$dummy')");
        Long studentId = studentMapper.selectList(null).get(0).getId();

        jdbc.execute("INSERT INTO course (name, credit) VALUES ('数学', 3)");
        Long courseId = jdbc.queryForObject("SELECT id FROM course WHERE name='数学'", Long.class);
        Long teacherId = jdbc.queryForObject("SELECT id FROM teacher WHERE name='admin'", Long.class);
        jdbc.execute("INSERT INTO opening (course_id, teacher_id, semester) VALUES (" + courseId + ", " + teacherId + ", '2024-1')");
        Long openingId = jdbc.queryForObject("SELECT MAX(id) FROM opening", Long.class);
        jdbc.execute("INSERT INTO selection (opening_id, student_id) VALUES (" + openingId + ", " + studentId + ")");
        Long selectionId = jdbc.queryForObject("SELECT MAX(id) FROM selection", Long.class);
        jdbc.execute("INSERT INTO grade (selection_id, score, semester) VALUES (" + selectionId + ", 90, '2024-1')");

        mockMvc.perform(delete("/api/admin/students/" + studentId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Integer selCount = jdbc.queryForObject("SELECT COUNT(*) FROM selection WHERE student_id=" + studentId, Integer.class);
        Integer gradeCount = jdbc.queryForObject("SELECT COUNT(*) FROM grade", Integer.class);
        assert selCount == 0;
        assert gradeCount == 0;
    }

    @Test
    void searchFuzzyByName() throws Exception {
        jdbc.execute("INSERT INTO student (name, password) VALUES ('张三', '$2a$10$dummy')");
        jdbc.execute("INSERT INTO student (name, password) VALUES ('张四', '$2a$10$dummy')");
        jdbc.execute("INSERT INTO student (name, password) VALUES ('李四', '$2a$10$dummy')");

        mockMvc.perform(get("/api/admin/students/search?name=张&fuzzy=true&page=1&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void nameValidation_rejectsEmpty() throws Exception {
        mockMvc.perform(post("/api/admin/students")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordValidation_rejectsShort() throws Exception {
        mockMvc.perform(post("/api/admin/students")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"张三\",\"password\":\"12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }
}
