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
public class CourseCrudTest {

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
        jdbc.execute("DELETE FROM student");
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
    void createCourse_success() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"高等数学\",\"credit\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("高等数学"))
                .andExpect(jsonPath("$.credit").value(4))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createCourse_validatesCreditRange_min() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"测试\",\"credit\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourse_validatesCreditRange_max() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"测试\",\"credit\":11}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourse_validatesNameLength() throws Exception {
        mockMvc.perform(post("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"A\",\"credit\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCourse() throws Exception {
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('数学', 3)");
        Long id = jdbc.queryForObject("SELECT id FROM course WHERE name='数学'", Long.class);

        mockMvc.perform(put("/api/admin/courses/" + id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"name\":\"线性代数\",\"credit\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("线性代数"))
                .andExpect(jsonPath("$.credit").value(2));
    }

    @Test
    void deleteCourse_cascadeDeletes() throws Exception {
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('物理', 4)");
        Long courseId = jdbc.queryForObject("SELECT id FROM course WHERE name='物理'", Long.class);
        Long teacherId = jdbc.queryForObject("SELECT id FROM teacher WHERE name='admin'", Long.class);
        jdbc.execute("INSERT INTO opening (course_id, teacher_id, semester) VALUES (" + courseId + ", " + teacherId + ", '2024-1')");
        Long openingId = jdbc.queryForObject("SELECT MAX(id) FROM opening", Long.class);

        jdbc.execute("INSERT INTO student (name, password) VALUES ('学生A', '$2a$10$dummy')");
        Long studentId = jdbc.queryForObject("SELECT id FROM student WHERE name='学生A'", Long.class);
        jdbc.execute("INSERT INTO selection (opening_id, student_id) VALUES (" + openingId + ", " + studentId + ")");
        Long selectionId = jdbc.queryForObject("SELECT MAX(id) FROM selection", Long.class);
        jdbc.execute("INSERT INTO grade (selection_id, score, semester) VALUES (" + selectionId + ", 85, '2024-1')");

        mockMvc.perform(delete("/api/admin/courses/" + courseId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Integer openingCount = jdbc.queryForObject("SELECT COUNT(*) FROM opening WHERE course_id=" + courseId, Integer.class);
        Integer selCount = jdbc.queryForObject("SELECT COUNT(*) FROM selection WHERE opening_id=" + openingId, Integer.class);
        Integer gradeCount = jdbc.queryForObject("SELECT COUNT(*) FROM grade WHERE selection_id=" + selectionId, Integer.class);
        assert openingCount == 0 : "openings should be deleted";
        assert selCount == 0 : "selections should be deleted";
        assert gradeCount == 0 : "grades should be deleted";
    }

    @Test
    void searchByName_fuzzy() throws Exception {
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('高等数学', 4)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('线性代数', 3)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('概率论', 2)");

        mockMvc.perform(get("/api/admin/courses/search?name=数&fuzzy=true")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void searchByCreditRange() throws Exception {
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('高等数学', 4)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('线性代数', 3)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('概率论', 2)");

        mockMvc.perform(get("/api/admin/courses/search?creditMin=3&creditMax=4")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void searchById() throws Exception {
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('数学', 3)");
        Long id = jdbc.queryForObject("SELECT id FROM course WHERE name='数学'", Long.class);

        mockMvc.perform(get("/api/admin/courses/search?id=" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.records[0].name").value("数学"));
    }

    @Test
    void listCourses_paginated() throws Exception {
        for (int i = 0; i < 15; i++) {
            jdbc.execute("INSERT INTO course (name, credit) VALUES ('课程" + i + "', 3)");
        }

        mockMvc.perform(get("/api/admin/courses?page=1&size=10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(10))
                .andExpect(jsonPath("$.total").value(15));
    }

    @Test
    void nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isUnauthorized());
    }
}
