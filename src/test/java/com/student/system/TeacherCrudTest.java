package com.student.system;

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
public class TeacherCrudTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("DELETE FROM grade");
        jdbc.execute("DELETE FROM selection");
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
    void createTeacher_withDefaultPassword() throws Exception {
        mockMvc.perform(post("/api/admin/teachers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"王老师\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("王老师"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void searchTeachers_returnsPaginatedWithoutPassword() throws Exception {
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('张老师', '$2a$10$dummy')");

        mockMvc.perform(get("/api/admin/teachers/search?name=张老师&fuzzy=false&page=1&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].name").value("张老师"))
                .andExpect(jsonPath("$.records[0].password").doesNotExist());
    }

    @Test
    void searchFuzzyByName() throws Exception {
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('张老师', '$2a$10$dummy')");
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('张副教授', '$2a$10$dummy')");
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('李老师', '$2a$10$dummy')");

        mockMvc.perform(get("/api/admin/teachers/search?name=张&fuzzy=true&page=1&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void updateTeacher() throws Exception {
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('刘老师', '$2a$10$dummy')");
        Long id = jdbc.queryForObject("SELECT id FROM teacher WHERE name='刘老师'", Long.class);

        mockMvc.perform(put("/api/admin/teachers/" + id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"刘教授\",\"password\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("刘教授"));
    }

    @Test
    void deleteTeacher_cascadeDeletesOpeningsSelectionsGrades() throws Exception {
        jdbc.execute("INSERT INTO student (name, password) VALUES ('学生A', '$2a$10$dummy')");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('物理', 4)");
        jdbc.execute("INSERT INTO teacher (name, password) VALUES ('陈老师', '$2a$10$dummy')");
        Long studentId = jdbc.queryForObject("SELECT id FROM student WHERE name='学生A'", Long.class);
        Long courseId = jdbc.queryForObject("SELECT id FROM course WHERE name='物理'", Long.class);
        Long teacherId = jdbc.queryForObject("SELECT id FROM teacher WHERE name='陈老师'", Long.class);
        jdbc.execute("INSERT INTO opening (course_id, teacher_id, semester) VALUES (" + courseId + ", " + teacherId + ", '2024-1')");
        Long openingId = jdbc.queryForObject("SELECT MAX(id) FROM opening", Long.class);
        jdbc.execute("INSERT INTO selection (opening_id, student_id) VALUES (" + openingId + ", " + studentId + ")");
        Long selectionId = jdbc.queryForObject("SELECT MAX(id) FROM selection", Long.class);
        jdbc.execute("INSERT INTO grade (selection_id, score, semester) VALUES (" + selectionId + ", 85, '2024-1')");

        mockMvc.perform(delete("/api/admin/teachers/" + teacherId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Integer openingCount = jdbc.queryForObject("SELECT COUNT(*) FROM opening WHERE teacher_id=" + teacherId, Integer.class);
        Integer selCount = jdbc.queryForObject("SELECT COUNT(*) FROM selection WHERE opening_id=" + openingId, Integer.class);
        Integer gradeCount = jdbc.queryForObject("SELECT COUNT(*) FROM grade WHERE selection_id=" + selectionId, Integer.class);
        assert openingCount == 0 : "openings should be deleted";
        assert selCount == 0 : "selections should be deleted";
        assert gradeCount == 0 : "grades should be deleted";
    }

    @Test
    void nameValidation_rejectsEmpty() throws Exception {
        mockMvc.perform(post("/api/admin/teachers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordValidation_rejectsShort() throws Exception {
        mockMvc.perform(post("/api/admin/teachers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"张老师\",\"password\":\"12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/teachers/search"))
                .andExpect(status().isUnauthorized());
    }
}
