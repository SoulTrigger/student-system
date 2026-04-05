package com.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Course;
import com.student.mapper.CourseMapper;
import com.student.mapper.GradeMapper;
import com.student.mapper.SelectionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CourseServiceTest {

    @Autowired private CourseService courseService;
    @Autowired private CourseMapper courseMapper;
    @Autowired private CascadeService cascadeService;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;

    @BeforeEach
    void cleanup() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        courseMapper.selectList(null).forEach(c -> courseMapper.deleteById(c.getId()));
    }

    private Course createCourse(String name, int credit) {
        Course c = new Course();
        c.setName(name);
        c.setCredit(credit);
        courseService.save(c);
        return c;
    }

    @Test
    void addCourse_autoIncrementId() {
        Course c = createCourse("高等数学", 4);
        assertNotNull(c.getId());
        assertTrue(c.getId() > 0);
    }

    @Test
    void creditRangeValidation_valid() {
        Course c1 = createCourse("课程A", 1);
        Course c2 = createCourse("课程B", 10);
        assertNotNull(c1.getId());
        assertNotNull(c2.getId());
    }

    @Test
    void fuzzyQueryByName() {
        createCourse("高等数学", 4);
        createCourse("线性代数", 3);
        createCourse("高等物理", 4);

        var wrapper = new LambdaQueryWrapper<Course>().like(Course::getName, "高等");
        long count = courseService.count(wrapper);
        assertEquals(2, count);
    }

    @Test
    void queryByCreditRange() {
        createCourse("课程A", 2);
        createCourse("课程B", 4);
        createCourse("课程C", 6);

        var wrapper = new LambdaQueryWrapper<Course>()
                .ge(Course::getCredit, 3)
                .le(Course::getCredit, 5);
        long count = courseService.count(wrapper);
        assertEquals(1, count);
    }

    @Test
    void deleteCourse_cascades() {
        Course c = createCourse("测试课程", 3);
        assertNotNull(courseMapper.selectById(c.getId()));
        courseService.removeById(c.getId());
        cascadeService.deleteCourseCascade(c.getId());
        assertNull(courseMapper.selectById(c.getId()));
    }

    @Test
    void updateCourse() {
        Course c = createCourse("旧名", 3);
        c.setName("新名");
        c.setCredit(5);
        courseService.updateById(c);
        Course db = courseMapper.selectById(c.getId());
        assertEquals("新名", db.getName());
        assertEquals(5, db.getCredit());
    }

    @Test
    void paginationWorks() {
        for (int i = 0; i < 15; i++) createCourse("课程" + i, (i % 10) + 1);
        Page<Course> page1 = courseService.page(new Page<>(1, 10));
        assertEquals(10, page1.getRecords().size());
        assertEquals(15, page1.getTotal());
    }
}
