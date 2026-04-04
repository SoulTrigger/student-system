package com.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Teacher;
import com.student.mapper.GradeMapper;
import com.student.mapper.SelectionMapper;
import com.student.mapper.TeacherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TeacherServiceTest {

    @Autowired private TeacherService teacherService;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private CascadeService cascadeService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;

    @BeforeEach
    void cleanup() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        teacherMapper.selectList(null).forEach(t -> teacherMapper.deleteById(t.getId()));
    }

    private Teacher createTeacher(String name) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setPassword(passwordEncoder.encode("123456"));
        teacherService.save(t);
        return t;
    }

    @Test
    void addTeacher_autoIncrementId() {
        Teacher t = createTeacher("王老师");
        assertNotNull(t.getId());
        assertTrue(t.getId() > 0);
    }

    @Test
    void addTeacher_passwordBcrypted() {
        Teacher t = createTeacher("李老师");
        Teacher db = teacherMapper.selectById(t.getId());
        assertNotEquals("123456", db.getPassword());
        assertTrue(passwordEncoder.matches("123456", db.getPassword()));
    }

    @Test
    void listTeachers_noPassword() {
        createTeacher("赵老师");
        Page<Teacher> page = teacherService.page(new Page<>(1, 10),
                new LambdaQueryWrapper<Teacher>()
                        .select(Teacher::getId, Teacher::getName, Teacher::getCreatedAt));
        assertNull(page.getRecords().get(0).getPassword());
    }

    @Test
    void fuzzyQueryByName() {
        createTeacher("王老师");
        createTeacher("王教授");
        createTeacher("李老师");

        var wrapper = new LambdaQueryWrapper<Teacher>().like(Teacher::getName, "王");
        long count = teacherService.count(wrapper);
        assertEquals(2, count);
    }

    @Test
    void deleteTeacher_cascades() {
        Teacher t = createTeacher("张老师");
        assertNotNull(teacherMapper.selectById(t.getId()));
        teacherService.removeById(t.getId());
        cascadeService.deleteTeacherCascade(t.getId());
        assertNull(teacherMapper.selectById(t.getId()));
    }

    @Test
    void updateTeacher() {
        Teacher t = createTeacher("旧名");
        t.setName("新名");
        teacherService.updateById(t);
        Teacher db = teacherMapper.selectById(t.getId());
        assertEquals("新名", db.getName());
    }

    @Test
    void paginationWorks() {
        for (int i = 0; i < 15; i++) createTeacher("教师" + i);
        Page<Teacher> page1 = teacherService.page(new Page<>(1, 10));
        assertEquals(10, page1.getRecords().size());
        assertEquals(15, page1.getTotal());
    }
}
