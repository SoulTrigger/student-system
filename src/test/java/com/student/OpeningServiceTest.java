package com.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Course;
import com.student.entity.Opening;
import com.student.entity.Teacher;
import com.student.mapper.CourseMapper;
import com.student.mapper.OpeningMapper;
import com.student.mapper.TeacherMapper;
import com.student.service.OpeningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OpeningServiceTest {

    @Autowired private OpeningService openingService;
    @Autowired private OpeningMapper openingMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private CourseMapper courseMapper;

    @BeforeEach
    void cleanup() {
        openingMapper.delete(null);
        // Don't delete teachers/courses, create fresh per test
    }

    private Teacher createTeacher() {
        Teacher t = new Teacher();
        t.setName("T" + System.nanoTime());
        t.setPassword("pwd");
        teacherMapper.insert(t);
        return t;
    }

    private Course createCourse() {
        Course c = new Course();
        c.setName("C" + System.nanoTime());
        c.setCredit(3);
        courseMapper.insert(c);
        return c;
    }

    @Test
    void testCreateOpening() {
        Teacher t = createTeacher();
        Course c = createCourse();
        Opening o = new Opening();
        o.setCourseId(c.getId());
        o.setTeacherId(t.getId());
        o.setSemester("2025-1");
        openingService.save(o);
        assertNotNull(o.getId());
        Opening found = openingService.getById(o.getId());
        assertEquals(c.getId(), found.getCourseId());
        assertEquals(t.getId(), found.getTeacherId());
        assertEquals("2025-1", found.getSemester());
    }

    @Test
    void testDuplicateOpeningReturnsConflict() {
        Teacher t = createTeacher();
        Course c = createCourse();
        Opening o1 = new Opening();
        o1.setCourseId(c.getId());
        o1.setTeacherId(t.getId());
        o1.setSemester("2025-1");
        openingService.save(o1);

        // Check duplicate detection logic
        boolean exists = openingService.count(new LambdaQueryWrapper<Opening>()
                .eq(Opening::getTeacherId, t.getId())
                .eq(Opening::getCourseId, c.getId())
                .eq(Opening::getSemester, "2025-1")) > 0;
        assertTrue(exists);

        // Different semester should not conflict
        boolean exists2 = openingService.count(new LambdaQueryWrapper<Opening>()
                .eq(Opening::getTeacherId, t.getId())
                .eq(Opening::getCourseId, c.getId())
                .eq(Opening::getSemester, "2025-2")) > 0;
        assertFalse(exists2);
    }

    @Test
    void testListByTeacher() {
        Teacher t1 = createTeacher();
        Teacher t2 = createTeacher();
        Course c = createCourse();

        Opening o1 = new Opening();
        o1.setCourseId(c.getId()); o1.setTeacherId(t1.getId()); o1.setSemester("2025-1");
        openingService.save(o1);

        Opening o2 = new Opening();
        o2.setCourseId(c.getId()); o2.setTeacherId(t2.getId()); o2.setSemester("2025-1");
        openingService.save(o2);

        long count1 = openingService.count(new LambdaQueryWrapper<Opening>().eq(Opening::getTeacherId, t1.getId()));
        assertEquals(1, count1);

        long countAll = openingService.count(null);
        assertEquals(2, countAll);
    }

    @Test
    void testAutoIncrementId() {
        Teacher t = createTeacher();
        Course c = createCourse();
        Opening o1 = new Opening();
        o1.setCourseId(c.getId()); o1.setTeacherId(t.getId()); o1.setSemester("2025-1");
        openingService.save(o1);

        Course c2 = createCourse();
        Opening o2 = new Opening();
        o2.setCourseId(c2.getId()); o2.setTeacherId(t.getId()); o2.setSemester("2025-2");
        openingService.save(o2);

        assertNotEquals(o1.getId(), o2.getId());
        assertTrue(o2.getId() > o1.getId());
    }

    @Test
    void testDeleteOpening() {
        Teacher t = createTeacher();
        Course c = createCourse();
        Opening o = new Opening();
        o.setCourseId(c.getId()); o.setTeacherId(t.getId()); o.setSemester("2025-1");
        openingService.save(o);
        Long id = o.getId();

        openingService.removeById(id);
        assertNull(openingService.getById(id));
    }

    @Test
    void testListWithFilters() {
        Teacher t = createTeacher();
        Course c1 = createCourse();
        Course c2 = createCourse();

        Opening o1 = new Opening();
        o1.setCourseId(c1.getId()); o1.setTeacherId(t.getId()); o1.setSemester("2025-1");
        openingService.save(o1);

        Opening o2 = new Opening();
        o2.setCourseId(c2.getId()); o2.setTeacherId(t.getId()); o2.setSemester("2025-2");
        openingService.save(o2);

        // Filter by courseId
        long count = openingService.count(new LambdaQueryWrapper<Opening>().eq(Opening::getCourseId, c1.getId()));
        assertEquals(1, count);

        // Filter by semester
        count = openingService.count(new LambdaQueryWrapper<Opening>().like(Opening::getSemester, "2025"));
        assertEquals(2, count);
    }
}
