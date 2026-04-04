package com.student.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.entity.*;
import com.student.mapper.*;
import com.student.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private OpeningMapper openingMapper;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;

    @BeforeEach
    void cleanup() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        openingMapper.selectList(null).forEach(o -> openingMapper.deleteById(o.getId()));
        courseMapper.selectList(null).forEach(c -> courseMapper.deleteById(c.getId()));
        studentMapper.selectList(null).forEach(s -> studentMapper.deleteById(s.getId()));
        teacherMapper.selectList(null).forEach(t -> teacherMapper.deleteById(t.getId()));
    }

    private Student insertStudent(String name) {
        Student s = new Student(); s.setName(name); s.setPassword(passwordEncoder.encode("pass"));
        studentMapper.insert(s); return s;
    }
    private Teacher insertTeacher(String name) {
        Teacher t = new Teacher(); t.setName(name); t.setPassword(passwordEncoder.encode("pass"));
        teacherMapper.insert(t); return t;
    }
    private Course insertCourse(String name, int credit) {
        Course c = new Course(); c.setName(name); c.setCredit(credit);
        courseMapper.insert(c); return c;
    }
    private Opening insertOpening(Long cid, Long tid, String sem) {
        Opening o = new Opening(); o.setCourseId(cid); o.setTeacherId(tid); o.setSemester(sem);
        openingMapper.insert(o); return o;
    }
    private Selection insertSelection(Long oid, Long sid) {
        Selection s = new Selection(); s.setOpeningId(oid); s.setStudentId(sid);
        selectionMapper.insert(s); return s;
    }
    private Grade insertGrade(Long selId, int score, String sem) {
        Grade g = new Grade(); g.setSelectionId(selId); g.setScore(score); g.setSemester(sem);
        gradeMapper.insert(g); return g;
    }
    private String studentToken(Long id) { return jwtUtil.generateToken(id, "student"); }
    private String teacherToken(Long id) { return jwtUtil.generateToken(id, "teacher"); }
    private String adminToken(Long id) { return jwtUtil.generateToken(id, "admin"); }

    private Map<?, ?> loginAndGetData(String id, String pw, String role) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", id, "password", pw, "role", role))))
                .andExpect(status().isOk()).andReturn();
        Map<?, ?> resp = objectMapper.readValue(r.getResponse().getContentAsString(), Map.class);
        return (Map<?, ?>) resp.get("data");
    }

    // ===== LOGIN FLOW =====

    @Test
    void fullLoginFlow_student() throws Exception {
        Student s = insertStudent("测试学生");
        Map<?, ?> data = loginAndGetData(String.valueOf(s.getId()), "pass", "student");
        assertNotNull(data.get("token"));
        assertEquals("student", data.get("role"));
        assertTrue(jwtUtil.isTokenValid((String) data.get("token")));

        mockMvc.perform(get("/api/selections/mine")
                .header("Authorization", "Bearer " + data.get("token")))
                .andExpect(status().isOk());
    }

    @Test
    void fullLoginFlow_teacher() throws Exception {
        Teacher t = insertTeacher("测试教师");
        Map<?, ?> data = loginAndGetData(String.valueOf(t.getId()), "pass", "teacher");
        assertEquals("teacher", data.get("role"));

        mockMvc.perform(get("/api/grades/teacher")
                .header("Authorization", "Bearer " + data.get("token")))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword() throws Exception {
        Student s = insertStudent("S1");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("id", String.valueOf(s.getId()), "password", "wrong", "role", "student"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void login_nonexistent() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("id", "99999", "password", "x", "role", "student"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));
    }

    // ===== CASCADE DELETE =====

    @Test
    void cascadeDelete_student() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId()); insertGrade(sel.getId(), 90, "2026-1");

        mockMvc.perform(delete("/api/students/" + s.getId())
                .header("Authorization", "Bearer " + adminToken(t.getId())))
                .andExpect(status().isOk());

        assertNull(studentMapper.selectById(s.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(teacherMapper.selectById(t.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }

    @Test
    void cascadeDelete_teacher() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1"); Teacher admin = insertTeacher("Admin");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId()); insertGrade(sel.getId(), 85, "2026-1");

        mockMvc.perform(delete("/api/teachers/" + t.getId())
                .header("Authorization", "Bearer " + adminToken(admin.getId())))
                .andExpect(status().isOk());

        assertNull(teacherMapper.selectById(t.getId()));
        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }

    @Test
    void cascadeDelete_course() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId()); insertGrade(sel.getId(), 75, "2026-1");

        mockMvc.perform(delete("/api/courses/" + c.getId())
                .header("Authorization", "Bearer " + adminToken(t.getId())))
                .andExpect(status().isOk());

        assertNull(courseMapper.selectById(c.getId()));
        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(teacherMapper.selectById(t.getId()));
    }

    @Test
    void cascadeDelete_opening() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId()); insertGrade(sel.getId(), 95, "2026-1");

        mockMvc.perform(delete("/api/openings/" + o.getId())
                .header("Authorization", "Bearer " + adminToken(t.getId())))
                .andExpect(status().isOk());

        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(teacherMapper.selectById(t.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }

    // ===== SELECTION CONSTRAINTS =====

    @Test
    void selectionConstraint_courseLimit8() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        for (int i = 0; i < 8; i++) {
            Course c = insertCourse("C" + i, 1);
            Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
            insertSelection(o.getId(), s.getId());
        }
        Course c9 = insertCourse("C9", 1);
        Opening o9 = insertOpening(c9.getId(), t.getId(), "2026-1");

        mockMvc.perform(post("/api/selections")
                .header("Authorization", "Bearer " + studentToken(s.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("openingId", o9.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("8")));
    }

    @Test
    void selectionConstraint_creditLimit24() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        int[] credits = {6, 6, 5, 5};
        for (int i = 0; i < credits.length; i++) {
            Course c = insertCourse("大课" + i, credits[i]);
            Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
            insertSelection(o.getId(), s.getId());
        }
        Course extra = insertCourse("额外", 3);
        Opening oExtra = insertOpening(extra.getId(), t.getId(), "2026-1");

        mockMvc.perform(post("/api/selections")
                .header("Authorization", "Bearer " + studentToken(s.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("openingId", oExtra.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("24")));
    }

    @Test
    void selectionConstraint_duplicate() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        insertSelection(o.getId(), s.getId());

        mockMvc.perform(post("/api/selections")
                .header("Authorization", "Bearer " + studentToken(s.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("openingId", o.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void selection_success() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");

        mockMvc.perform(post("/api/selections")
                .header("Authorization", "Bearer " + studentToken(s.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("openingId", o.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.openingId").value(o.getId()));
    }

    @Test
    void dropCourse_success() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId());

        mockMvc.perform(delete("/api/selections/" + sel.getId())
                .header("Authorization", "Bearer " + studentToken(s.getId())))
                .andExpect(status().isOk());

        assertNull(selectionMapper.selectById(sel.getId()));
    }

    // ===== BATCH GRADE =====

    @Test
    void batchGrade_success() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId());

        List<Map<String, Object>> grades = List.of(
                Map.of("selectionId", sel.getId(), "score", 85, "semester", "2026-1"));

        mockMvc.perform(post("/api/grades/batch")
                .header("Authorization", "Bearer " + teacherToken(t.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(grades)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].score").value(85));
    }

    @Test
    void batchGrade_invalidScore() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId());

        mockMvc.perform(post("/api/grades/batch")
                .header("Authorization", "Bearer " + teacherToken(t.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        List.of(Map.of("selectionId", sel.getId(), "score", 150, "semester", "2026-1")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void batchGrade_studentForbidden() throws Exception {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        Course c = insertCourse("Math", 3); Opening o = insertOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = insertSelection(o.getId(), s.getId());

        mockMvc.perform(post("/api/grades/batch")
                .header("Authorization", "Bearer " + studentToken(s.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        List.of(Map.of("selectionId", sel.getId(), "score", 85, "semester", "2026-1")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    // ===== SECURITY =====

    @Test
    void noPlaintextPasswords() {
        Student s = insertStudent("S1"); Teacher t = insertTeacher("T1");
        assertTrue(s.getPassword().startsWith("$2a$"));
        assertTrue(t.getPassword().startsWith("$2a$"));
        assertTrue(passwordEncoder.matches("pass", s.getPassword()));
        assertTrue(passwordEncoder.matches("pass", t.getPassword()));
    }

    @Test
    void changePasswordFlow() throws Exception {
        Student s = insertStudent("S1");
        Map<?, ?> data = loginAndGetData(String.valueOf(s.getId()), "pass", "student");
        String token = (String) data.get("token");

        mockMvc.perform(put("/api/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("oldPassword", "pass", "newPassword", "newpass123"))))
                .andExpect(status().isOk());

        // Old password fails
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("id", String.valueOf(s.getId()), "password", "pass", "role", "student"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));

        // New password works
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("id", String.valueOf(s.getId()), "password", "newpass123", "role", "student"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void sqlInjection_safe() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("id", "1 OR 1=1", "password", "anything", "role", "student"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(401));

        Course c = new Course(); c.setName("Test'; DROP TABLE student; --"); c.setCredit(3);
        courseMapper.insert(c);
        assertNotNull(courseMapper.selectById(c.getId()));
    }
}
