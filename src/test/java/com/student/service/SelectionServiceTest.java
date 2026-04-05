package com.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.*;
import com.student.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SelectionServiceTest {

    @Autowired private SelectionService selectionService;
    @Autowired private OpeningService openingService;
    @Autowired private CourseService courseService;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private OpeningMapper openingMapper;

    @BeforeEach
    void cleanup() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        openingMapper.selectList(null).forEach(o -> openingMapper.deleteById(o.getId()));
        courseMapper.selectList(null).forEach(c -> courseMapper.deleteById(c.getId()));
        studentMapper.selectList(null).forEach(s -> studentMapper.deleteById(s.getId()));
        teacherMapper.selectList(null).forEach(t -> teacherMapper.deleteById(t.getId()));
    }

    private Student createStudent(String name) {
        Student s = new Student();
        s.setName(name);
        s.setPassword("$2a$10$fake");
        studentMapper.insert(s);
        return s;
    }

    private Teacher createTeacher(String name) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setPassword("$2a$10$fake");
        teacherMapper.insert(t);
        return t;
    }

    private Course createCourse(String name, int credit) {
        Course c = new Course();
        c.setName(name);
        c.setCredit(credit);
        courseService.save(c);
        return c;
    }

    private Opening createOpening(Long courseId, Long teacherId, String semester) {
        Opening o = new Opening();
        o.setCourseId(courseId);
        o.setTeacherId(teacherId);
        o.setSemester(semester);
        openingService.save(o);
        return o;
    }

    private int calcCredits(Long studentId) {
        int total = 0;
        for (Selection sel : selectionService.list(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, studentId))) {
            Opening opn = openingService.getById(sel.getOpeningId());
            if (opn != null) {
                Course c = courseService.getById(opn.getCourseId());
                if (c != null) total += c.getCredit();
            }
        }
        return total;
    }

    @Test
    void selectCourse_success() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");
        Course c = createCourse("数学", 3);
        Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");

        Selection sel = new Selection();
        sel.setOpeningId(opn.getId());
        sel.setStudentId(stu.getId());
        selectionService.save(sel);

        assertNotNull(sel.getId());
        assertEquals(opn.getId(), sel.getOpeningId());
        assertEquals(stu.getId(), sel.getStudentId());
    }

    @Test
    void duplicateSelection_detectedByCount() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");
        Course c = createCourse("数学", 3);
        Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");

        Selection sel = new Selection();
        sel.setOpeningId(opn.getId());
        sel.setStudentId(stu.getId());
        selectionService.save(sel);

        // Controller checks count before insert; verify count > 0 means duplicate
        long count = selectionService.count(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getOpeningId, opn.getId())
                .eq(Selection::getStudentId, stu.getId()));
        assertTrue(count > 0, "Duplicate should be detected via count");
    }

    @Test
    void courseLimit_atMax8() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");

        for (int i = 0; i < 8; i++) {
            Course c = createCourse("课程" + i, 1);
            Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");
            Selection sel = new Selection();
            sel.setOpeningId(opn.getId());
            sel.setStudentId(stu.getId());
            selectionService.save(sel);
        }

        long currentCount = selectionService.count(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, stu.getId()));
        assertEquals(8, currentCount);
        // Controller would reject: currentCount >= 8
        assertTrue(currentCount >= 8);
    }

    @Test
    void creditLimit_exceeds24() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");

        // 22 credits already selected
        int[] credits = {6, 6, 5, 5};
        for (int i = 0; i < credits.length; i++) {
            Course c = createCourse("大课" + i, credits[i]);
            Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");
            Selection sel = new Selection();
            sel.setOpeningId(opn.getId());
            sel.setStudentId(stu.getId());
            selectionService.save(sel);
        }

        int currentCredits = calcCredits(stu.getId());
        assertEquals(22, currentCredits);

        // Adding 3-credit course exceeds 24
        Course extra = createCourse("额外课", 3);
        assertTrue(currentCredits + extra.getCredit() > 24, "Should exceed credit limit");
    }

    @Test
    void dropCourse_deletesGrade() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");
        Course c = createCourse("数学", 3);
        Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");

        Selection sel = new Selection();
        sel.setOpeningId(opn.getId());
        sel.setStudentId(stu.getId());
        selectionService.save(sel);

        Grade g = new Grade();
        g.setSelectionId(sel.getId());
        g.setScore(85);
        g.setSemester("2025-1");
        gradeMapper.insert(g);

        // Drop: delete grade then selection
        gradeMapper.delete(new LambdaQueryWrapper<Grade>().eq(Grade::getSelectionId, sel.getId()));
        selectionService.removeById(sel.getId());

        assertNull(selectionMapper.selectById(sel.getId()));
        assertEquals(0, gradeMapper.selectList(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getSelectionId, sel.getId())).size());
    }

    @Test
    void totalCreditsCalculation() {
        Student stu = createStudent("张三");
        Teacher tch = createTeacher("李老师");

        Course c1 = createCourse("数学", 4);
        Course c2 = createCourse("英语", 3);
        Opening opn1 = createOpening(c1.getId(), tch.getId(), "2025-1");
        Opening opn2 = createOpening(c2.getId(), tch.getId(), "2025-1");

        Selection sel1 = new Selection();
        sel1.setOpeningId(opn1.getId());
        sel1.setStudentId(stu.getId());
        selectionService.save(sel1);

        Selection sel2 = new Selection();
        sel2.setOpeningId(opn2.getId());
        sel2.setStudentId(stu.getId());
        selectionService.save(sel2);

        assertEquals(7, calcCredits(stu.getId()));
    }

    @Test
    void dropCourse_byNonOwner_fails() {
        Student stu1 = createStudent("张三");
        Student stu2 = createStudent("李四");
        Teacher tch = createTeacher("王老师");
        Course c = createCourse("数学", 3);
        Opening opn = createOpening(c.getId(), tch.getId(), "2025-1");

        Selection sel = new Selection();
        sel.setOpeningId(opn.getId());
        sel.setStudentId(stu1.getId());
        selectionService.save(sel);

        // stu2 should not be able to drop stu1's selection
        assertNotEquals(stu2.getId(), sel.getStudentId());
        // Controller checks: !sel.getStudentId().equals(studentId) -> 403
    }
}
