package com.student.service;

import com.student.entity.*;
import com.student.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CascadeServiceTest {

    @Autowired private CascadeService cascadeService;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private OpeningMapper openingMapper;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;

    private Student saveStudent(String name) {
        Student s = new Student();
        s.setName(name);
        s.setPassword("pass");
        studentMapper.insert(s);
        return s;
    }

    private Teacher saveTeacher(String name) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setPassword("pass");
        teacherMapper.insert(t);
        return t;
    }

    private Course saveCourse(String name, int credit) {
        Course c = new Course();
        c.setName(name);
        c.setCredit(credit);
        courseMapper.insert(c);
        return c;
    }

    private Opening saveOpening(Long courseId, Long teacherId, String semester) {
        Opening o = new Opening();
        o.setCourseId(courseId);
        o.setTeacherId(teacherId);
        o.setSemester(semester);
        openingMapper.insert(o);
        return o;
    }

    private Selection saveSelection(Long openingId, Long studentId) {
        Selection s = new Selection();
        s.setOpeningId(openingId);
        s.setStudentId(studentId);
        selectionMapper.insert(s);
        return s;
    }

    private Grade saveGrade(Long selectionId, Integer score, String semester) {
        Grade g = new Grade();
        g.setSelectionId(selectionId);
        g.setScore(score);
        g.setSemester(semester);
        gradeMapper.insert(g);
        return g;
    }

    @BeforeEach
    void cleanUp() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        openingMapper.selectList(null).forEach(o -> openingMapper.deleteById(o.getId()));
        courseMapper.selectList(null).forEach(c -> courseMapper.deleteById(c.getId()));
        studentMapper.selectList(null).forEach(s -> studentMapper.deleteById(s.getId()));
        teacherMapper.selectList(null).forEach(t -> teacherMapper.deleteById(t.getId()));
    }

    @Test
    void deleteStudentCascade_removesSelectionsAndGrades() {
        Student s = saveStudent("S1");
        Teacher t = saveTeacher("T1");
        Course c = saveCourse("Math", 3);
        Opening o = saveOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = saveSelection(o.getId(), s.getId());
        saveGrade(sel.getId(), 90, "2026-1");

        cascadeService.deleteStudentCascade(s.getId());

        assertNull(studentMapper.selectById(s.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        // Teacher, course, opening should still exist
        assertNotNull(teacherMapper.selectById(t.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }

    @Test
    void deleteTeacherCascade_removesOpeningsSelectionsAndGrades() {
        Student s = saveStudent("S1");
        Teacher t = saveTeacher("T1");
        Course c = saveCourse("Math", 3);
        Opening o = saveOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = saveSelection(o.getId(), s.getId());
        saveGrade(sel.getId(), 85, "2026-1");

        cascadeService.deleteTeacherCascade(t.getId());

        assertNull(teacherMapper.selectById(t.getId()));
        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }

    @Test
    void deleteCourseCascade_removesOpeningsSelectionsAndGrades() {
        Student s = saveStudent("S1");
        Teacher t = saveTeacher("T1");
        Course c = saveCourse("Math", 3);
        Opening o = saveOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = saveSelection(o.getId(), s.getId());
        saveGrade(sel.getId(), 75, "2026-1");

        cascadeService.deleteCourseCascade(c.getId());

        assertNull(courseMapper.selectById(c.getId()));
        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(teacherMapper.selectById(t.getId()));
    }

    @Test
    void deleteOpeningCascade_removesSelectionsAndGrades() {
        Student s = saveStudent("S1");
        Teacher t = saveTeacher("T1");
        Course c = saveCourse("Math", 3);
        Opening o = saveOpening(c.getId(), t.getId(), "2026-1");
        Selection sel = saveSelection(o.getId(), s.getId());
        saveGrade(sel.getId(), 95, "2026-1");

        cascadeService.deleteOpeningCascade(o.getId());

        assertNull(openingMapper.selectById(o.getId()));
        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(null).size());
        // Student, teacher, course should still exist
        assertNotNull(studentMapper.selectById(s.getId()));
        assertNotNull(teacherMapper.selectById(t.getId()));
        assertNotNull(courseMapper.selectById(c.getId()));
    }
}
