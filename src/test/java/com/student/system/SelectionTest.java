package com.student.system;

import com.student.system.util.JwtUtil;
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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SelectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String studentToken;
    private Long studentId;

    @BeforeEach
    void setUp() {
        // Clean up test data
        jdbcTemplate.execute("DELETE FROM grade");
        jdbcTemplate.execute("DELETE FROM selection");
        jdbcTemplate.execute("DELETE FROM opening");
        jdbcTemplate.execute("DELETE FROM course WHERE id > 100");
        jdbcTemplate.execute("DELETE FROM teacher WHERE id > 100");
        jdbcTemplate.execute("DELETE FROM student WHERE id > 100");

        // Create test student
        jdbcTemplate.execute("INSERT INTO student (id, name, password) VALUES (101, '测试学生', '$2a$10$dummyhashfortest')");
        studentId = 101L;
        studentToken = jwtUtil.generateToken(studentId, "student", "测试学生");

        // Create test teacher
        jdbcTemplate.execute("INSERT INTO teacher (id, name, password) VALUES (101, '测试教师', '$2a$10$dummyhashfortest')");

        // Create test courses
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (101, '高等数学', 4)");
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (102, '线性代数', 3)");
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (103, '大学物理', 5)");

        // Create openings
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (101, 101, 101, '2026下学期')");
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (102, 102, 101, '2026下学期')");
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (103, 103, 101, '2026下学期')");
    }

    @Test
    void testListAvailable() throws Exception {
        mockMvc.perform(get("/api/student/selections/available")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(3));
    }

    @Test
    void testSelectCourse() throws Exception {
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":101}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingId").value(101));

        // Verify available is now 2
        mockMvc.perform(get("/api/student/selections/available")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(2));
    }

    @Test
    void testCannotSelectTwice() throws Exception {
        // First selection succeeds
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":101}"))
                .andExpect(status().isOk());

        // Second selection fails
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("已经选过该课程"));
    }

    @Test
    void testCreditLimit() throws Exception {
        // Create courses totalling > 24 credits
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (104, '大课1', 10)");
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (105, '大课2', 10)");
        jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (106, '大课3', 10)");
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (104, 104, 101, '2026下学期')");
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (105, 105, 101, '2026下学期')");
        jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (106, 106, 101, '2026下学期')");

        // Select 2 courses = 20 credits
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":104}")).andExpect(status().isOk());
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":105}")).andExpect(status().isOk());

        // Third course would exceed 24 (20 + 10 = 30)
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":106}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("总学分不能超过24"));
    }

    @Test
    void testCourseLimit() throws Exception {
        // Create 8+ openings with 1-credit courses
        for (int i = 110; i < 120; i++) {
            jdbcTemplate.execute("INSERT INTO course (id, name, credit) VALUES (" + i + ", '课" + i + "', 1)");
            jdbcTemplate.execute("INSERT INTO opening (id, course_id, teacher_id, semester) VALUES (" + i + ", " + i + ", 101, '2026下学期')");
        }

        // Select 8 courses
        for (int i = 110; i < 118; i++) {
            mockMvc.perform(post("/api/student/selections")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType("application/json")
                    .content("{\"openingId\":" + i + "}"))
                    .andExpect(status().isOk());
        }

        // 9th should fail
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":118}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("每学期最多选8门课"));
    }

    @Test
    void testWithdraw() throws Exception {
        // Select a course
        MvcResult result = mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":101}"))
                .andExpect(status().isOk())
                .andReturn();

        // Get selection ID from response
        String response = result.getResponse().getContentAsString();
        Long selectionId = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .build().readTree(response).get("id").asLong();

        // Add a grade for this selection
        jdbcTemplate.execute("INSERT INTO grade (selection_id, score, semester) VALUES (" + selectionId + ", 90, '2026下学期')");

        // Verify grade exists
        Integer gradeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM grade WHERE selection_id = " + selectionId, Integer.class);
        assertEquals(1, gradeCount);

        // Withdraw
        mockMvc.perform(delete("/api/student/selections/" + selectionId)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // Verify grade was also deleted
        gradeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM grade WHERE selection_id = " + selectionId, Integer.class);
        assertEquals(0, gradeCount);
    }

    @Test
    void testListMineWithCredits() throws Exception {
        // Select two courses (4 + 3 = 7 credits)
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":101}")).andExpect(status().isOk());
        mockMvc.perform(post("/api/student/selections")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"openingId\":102}")).andExpect(status().isOk());

        mockMvc.perform(get("/api/student/selections/mine")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selections.records.length()").value(2))
                .andExpect(jsonPath("$.totalCredits").value(7));
    }

    @Test
    void testFuzzySearch() throws Exception {
        mockMvc.perform(get("/api/student/selections/available?courseName=数学&fuzzy=true")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1));
    }

    @Test
    void testNonStudentForbidden() throws Exception {
        String adminToken = jwtUtil.generateToken(1L, "admin", "admin");
        mockMvc.perform(get("/api/student/selections/available")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }
}
