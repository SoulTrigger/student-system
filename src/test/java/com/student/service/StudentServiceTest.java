package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Student;
import com.student.mapper.GradeMapper;
import com.student.mapper.SelectionMapper;
import com.student.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StudentServiceTest {

    @Autowired private StudentService studentService;
    @Autowired private StudentMapper studentMapper;
    @Autowired private CascadeService cascadeService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SelectionMapper selectionMapper;
    @Autowired private GradeMapper gradeMapper;

    @BeforeEach
    void cleanup() {
        gradeMapper.selectList(null).forEach(g -> gradeMapper.deleteById(g.getId()));
        selectionMapper.selectList(null).forEach(s -> selectionMapper.deleteById(s.getId()));
        studentMapper.selectList(null).forEach(s -> studentMapper.deleteById(s.getId()));
    }

    private Student createStudent(String name) {
        Student s = new Student();
        s.setName(name);
        s.setPassword(passwordEncoder.encode("123456"));
        studentService.save(s);
        return s;
    }

    @Test
    void addStudent_autoIncrementId() {
        Student s = createStudent("张三");
        assertNotNull(s.getId());
        assertTrue(s.getId() > 0);
    }

    @Test
    void addStudent_passwordBcrypted() {
        Student s = createStudent("李四");
        Student db = studentMapper.selectById(s.getId());
        assertNotEquals("123456", db.getPassword());
        assertTrue(passwordEncoder.matches("123456", db.getPassword()));
    }

    @Test
    void listStudents_noPassword() {
        createStudent("王五");
        Page<Student> page = studentService.page(new Page<>(1, 10),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .select(Student::getId, Student::getName, Student::getCreatedAt));
        assertNull(page.getRecords().get(0).getPassword());
    }

    @Test
    void fuzzyQueryByName() {
        createStudent("张三");
        createStudent("张四");
        createStudent("李五");

        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .like(Student::getName, "张");
        long count = studentService.count(wrapper);
        assertEquals(2, count);
    }

    @Test
    void deleteStudent_cascades() {
        Student s = createStudent("赵六");
        // Verify student exists then delete
        assertNotNull(studentMapper.selectById(s.getId()));
        studentService.removeById(s.getId());
        cascadeService.deleteStudentCascade(s.getId());
        assertNull(studentMapper.selectById(s.getId()));
    }

    @Test
    void updateStudent() {
        Student s = createStudent("旧名");
        s.setName("新名");
        studentService.updateById(s);
        Student db = studentMapper.selectById(s.getId());
        assertEquals("新名", db.getName());
    }

    @Test
    void paginationWorks() {
        for (int i = 0; i < 15; i++) createStudent("学生" + i);
        Page<Student> page1 = studentService.page(new Page<>(1, 10));
        assertEquals(10, page1.getRecords().size());
        assertEquals(15, page1.getTotal());
    }
}
