package com.student.service;

import com.student.entity.*;
import com.student.mapper.GradeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GradeServiceTest {

    @Autowired private GradeService gradeService;
    @Autowired private StudentService studentService;
    @Autowired private TeacherService teacherService;
    @Autowired private CourseService courseService;
    @Autowired private OpeningService openingService;
    @Autowired private SelectionService selectionService;
    @Autowired private GradeMapper gradeMapper;

    private Long studentId, teacherId, courseId, openingId, selectionId;

    @BeforeEach
    void setup() {
        gradeMapper.delete(null);
        selectionService.remove(null);
        openingService.remove(null);
        courseService.remove(null);
        studentService.remove(null);
        teacherService.remove(null);

        Student stu = new Student();
        stu.setName("测试学生");
        stu.setPassword("123456");
        studentService.save(stu);
        studentId = stu.getId();

        Teacher tch = new Teacher();
        tch.setName("测试教师");
        tch.setPassword("123456");
        teacherService.save(tch);
        teacherId = tch.getId();

        Course crs = new Course();
        crs.setName("数学");
        crs.setCredit(4);
        courseService.save(crs);
        courseId = crs.getId();

        Opening opn = new Opening();
        opn.setCourseId(courseId);
        opn.setTeacherId(teacherId);
        opn.setSemester("2025-1");
        openingService.save(opn);
        openingId = opn.getId();

        Selection sel = new Selection();
        sel.setOpeningId(openingId);
        sel.setStudentId(studentId);
        selectionService.save(sel);
        selectionId = sel.getId();
    }

    @Test
    void batchSave_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(85);
        g.setSemester("2025-1");

        List<Grade> saved = gradeService.batchSave(List.of(g));
        assertEquals(1, saved.size());
        assertNotNull(saved.get(0).getId());
        assertEquals(85, saved.get(0).getScore());
    }

    @Test
    void batchSave_invalidScore_low() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(-1);
        g.setSemester("2025-1");
        assertThrows(IllegalArgumentException.class, () -> gradeService.batchSave(List.of(g)));
    }

    @Test
    void batchSave_invalidScore_high() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(101);
        g.setSemester("2025-1");
        assertThrows(IllegalArgumentException.class, () -> gradeService.batchSave(List.of(g)));
    }

    @Test
    void batchSave_invalidSelection() {
        Grade g = new Grade();
        g.setSelectionId(99999L);
        g.setScore(80);
        g.setSemester("2025-1");
        assertThrows(IllegalArgumentException.class, () -> gradeService.batchSave(List.of(g)));
    }

    @Test
    void batchSave_boundaryScores() {
        Grade g0 = new Grade();
        g0.setSelectionId(selectionId);
        g0.setScore(0);
        g0.setSemester("2025-1");

        Grade g100 = new Grade();
        g100.setSelectionId(selectionId);
        g100.setScore(100);
        g100.setSemester("2025-1");

        // Save one at a time since same selection can't have duplicate grades easily
        List<Grade> saved = gradeService.batchSave(List.of(g0));
        assertEquals(0, saved.get(0).getScore());

        // Create another selection for the 100 score test
        Course crs2 = new Course();
        crs2.setName("英语");
        crs2.setCredit(3);
        courseService.save(crs2);
        Opening opn2 = new Opening();
        opn2.setCourseId(crs2.getId());
        opn2.setTeacherId(teacherId);
        opn2.setSemester("2025-1");
        openingService.save(opn2);
        Selection sel2 = new Selection();
        sel2.setOpeningId(opn2.getId());
        sel2.setStudentId(studentId);
        selectionService.save(sel2);

        Grade g100b = new Grade();
        g100b.setSelectionId(sel2.getId());
        g100b.setScore(100);
        g100b.setSemester("2025-1");
        saved = gradeService.batchSave(List.of(g100b));
        assertEquals(100, saved.get(0).getScore());
    }

    @Test
    void listByTeacher_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(90);
        g.setSemester("2025-1");
        gradeService.save(g);

        List<Map<String, Object>> list = gradeService.listByTeacher(teacherId, null);
        assertFalse(list.isEmpty());
        assertEquals(90, list.get(0).get("score"));
        assertEquals("测试学生", list.get(0).get("studentName"));
        assertEquals("数学", list.get(0).get("courseName"));
    }

    @Test
    void listByTeacher_filteredBySemester() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(90);
        g.setSemester("2025-1");
        gradeService.save(g);

        List<Map<String, Object>> list = gradeService.listByTeacher(teacherId, "2025-2");
        assertTrue(list.isEmpty());
    }

    @Test
    void listAll_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(75);
        g.setSemester("2025-1");
        gradeService.save(g);

        List<Map<String, Object>> list = gradeService.listAll(null);
        assertFalse(list.isEmpty());
        assertEquals(75, list.get(0).get("score"));
    }

    @Test
    void updateScore_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(60);
        g.setSemester("2025-1");
        gradeService.save(g);

        gradeService.updateScore(g.getId(), 95, "admin");
        Grade updated = gradeService.getById(g.getId());
        assertEquals(95, updated.getScore());
    }

    @Test
    void updateScore_invalidRange() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(60);
        g.setSemester("2025-1");
        gradeService.save(g);

        assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateScore(g.getId(), 150, "admin"));
    }

    @Test
    void updateScore_notFound() {
        assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateScore(99999L, 80, "admin"));
    }

    @Test
    void listByStudent_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(88);
        g.setSemester("2025-1");
        gradeService.save(g);

        List<Map<String, Object>> list = gradeService.listByStudent(studentId);
        assertFalse(list.isEmpty());
        assertEquals(88, list.get(0).get("score"));
        assertEquals("数学", list.get(0).get("courseName"));
    }

    @Test
    void studentAverage_success() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(80);
        g.setSemester("2025-1");
        gradeService.save(g);

        // Add another course
        Course crs2 = new Course();
        crs2.setName("物理");
        crs2.setCredit(3);
        courseService.save(crs2);
        Opening opn2 = new Opening();
        opn2.setCourseId(crs2.getId());
        opn2.setTeacherId(teacherId);
        opn2.setSemester("2025-1");
        openingService.save(opn2);
        Selection sel2 = new Selection();
        sel2.setOpeningId(opn2.getId());
        sel2.setStudentId(studentId);
        selectionService.save(sel2);

        Grade g2 = new Grade();
        g2.setSelectionId(sel2.getId());
        g2.setScore(90);
        g2.setSemester("2025-1");
        gradeService.save(g2);

        Map<String, Object> avg = gradeService.studentAverage(studentId);
        assertEquals(85.0, (Double) avg.get("averageScore"));
        assertEquals(2, avg.get("courseCount"));
    }

    @Test
    void studentAverage_noGrades() {
        Map<String, Object> avg = gradeService.studentAverage(studentId);
        assertEquals(0.0, (Double) avg.get("averageScore"));
        assertEquals(0, avg.get("courseCount"));
    }

    @Test
    void studentAverage_decimalRounding() {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(85);
        g.setSemester("2025-1");
        gradeService.save(g);

        // avg of one grade = 85.0
        Map<String, Object> avg = gradeService.studentAverage(studentId);
        assertEquals(85.0, (Double) avg.get("averageScore"));
    }
}
