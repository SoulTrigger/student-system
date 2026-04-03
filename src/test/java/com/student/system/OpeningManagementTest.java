package com.student.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OpeningManagementTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Long teacherId;
    private String teacherToken;
    private Long courseId;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("DELETE FROM grade");
        jdbc.execute("DELETE FROM selection");
        jdbc.execute("DELETE FROM opening");
        jdbc.execute("DELETE FROM course");
        jdbc.execute("DELETE FROM teacher WHERE name != 'admin'");
        jdbc.execute("DELETE FROM student");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // Get admin token
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"userId\":\"1\",\"password\":\"admin123\",\"role\":\"admin\"}"))
                .andExpect(status().isOk()).andReturn();
        adminToken = parseToken(result);

        // Create teacher with known password
        jdbc.update("INSERT INTO teacher (name, password) VALUES ('张老师', ?)", passwordEncoder.encode("test123"));
        teacherId = jdbc.queryForObject("SELECT id FROM teacher WHERE name='张老师'", Long.class);

        // Login as teacher
        result = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"userId\":\"" + teacherId + "\",\"password\":\"test123\",\"role\":\"teacher\"}"))
                .andExpect(status().isOk()).andReturn();
        teacherToken = parseToken(result);

        // Create courses
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('高等数学', 4)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('线性代数', 3)");
        jdbc.execute("INSERT INTO course (name, credit) VALUES ('大学英语', 2)");
        courseId = jdbc.queryForObject("SELECT id FROM course WHERE name='高等数学'", Long.class);
    }

    private String parseToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(body).get("token").asText();
    }

    @Test
    void teacherCreateOpening_success() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.teacherId").value(teacherId))
                .andExpect(jsonPath("$.semester").exists())
                .andExpect(jsonPath("$.courseName").value("高等数学"))
                .andExpect(jsonPath("$.teacherName").value("张老师"));
    }

    @Test
    void teacherCannotOpenSameCourseTwiceInSameSemester() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("本学期已开过该课程"));
    }

    @Test
    void teacherSeesOnlyOwnOpenings() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1))
                .andExpect(jsonPath("$.records[0].teacherId").value(teacherId));
    }

    @Test
    void teacherAvailableCourses_excludesOpened() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teacher/courses/available")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void adminSearchOpenings_byCourseName_fuzzy() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/openings/search")
                .param("courseName", "高等")
                .param("fuzzy", "true")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1));
    }

    @Test
    void adminSearchOpenings_byTeacherName_exact() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/openings/search")
                .param("teacherName", "张老师")
                .param("fuzzy", "false")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1));
    }

    @Test
    void adminDeleteOpening_cascadeDeletesSelectionsAndGrades() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + teacherToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isOk()).andReturn();
        Long openingId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Create student, selection, grade
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("INSERT INTO student (name, password) VALUES ('李同学', '$2a$10$dummy')");
        Long studentId = jdbc.queryForObject("SELECT id FROM student WHERE name='李同学'", Long.class);
        jdbc.execute("INSERT INTO selection (opening_id, student_id) VALUES (" + openingId + ", " + studentId + ")");
        Long selectionId = jdbc.queryForObject("SELECT id FROM selection WHERE opening_id=" + openingId, Long.class);
        jdbc.execute("INSERT INTO grade (selection_id, score, semester) VALUES (" + selectionId + ", 85, '2026上学期')");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");

        mockMvc.perform(delete("/api/admin/openings/" + openingId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));

        assert jdbc.queryForObject("SELECT COUNT(*) FROM grade WHERE selection_id=" + selectionId, Integer.class) == 0;
        assert jdbc.queryForObject("SELECT COUNT(*) FROM selection WHERE opening_id=" + openingId, Integer.class) == 0;
        assert jdbc.queryForObject("SELECT COUNT(*) FROM opening WHERE id=" + openingId, Integer.class) == 0;
    }

    @Test
    void adminSearchRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/openings/search")
                .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherOpeningRequiresTeacherRole() throws Exception {
        mockMvc.perform(post("/api/teacher/openings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content("{\"courseId\":" + courseId + "}"))
                .andExpect(status().isForbidden());
    }
}
