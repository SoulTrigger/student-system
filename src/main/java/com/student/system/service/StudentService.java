package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.StudentRequest;
import com.student.system.dto.StudentResponse;
import com.student.system.entity.Grade;
import com.student.system.entity.Selection;
import com.student.system.entity.Student;
import com.student.system.mapper.GradeMapper;
import com.student.system.mapper.SelectionMapper;
import com.student.system.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<StudentResponse> list(int page, int size) {
        Page<Student> studentPage = new Page<>(page, size);
        Page<Student> result = studentMapper.selectPage(studentPage, new QueryWrapper<Student>().orderByDesc("id"));
        Page<StudentResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        Student student = new Student();
        student.setName(request.getName());
        String pwd = (request.getPassword() != null && !request.getPassword().isEmpty())
                ? request.getPassword() : "123456";
        student.setPassword(passwordEncoder.encode(pwd));
        studentMapper.insert(student);
        return toResponse(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            student.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        studentMapper.updateById(student);
        return toResponse(student);
    }

    @Transactional
    public void delete(Long id) {
        List<Selection> selections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("student_id", id));
        for (Selection sel : selections) {
            gradeMapper.delete(new QueryWrapper<Grade>().eq("selection_id", sel.getId()));
        }
        selectionMapper.delete(new QueryWrapper<Selection>().eq("student_id", id));
        studentMapper.deleteById(id);
    }

    public Page<StudentResponse> search(String id, String name, boolean fuzzy, int page, int size) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        if (id != null && !id.isEmpty()) {
            try {
                wrapper.eq("id", Long.parseLong(id));
            } catch (NumberFormatException e) { }
        }
        if (name != null && !name.isEmpty()) {
            if (fuzzy) {
                wrapper.like("name", name);
            } else {
                wrapper.eq("name", name);
            }
        }
        wrapper.orderByDesc("id");
        Page<Student> result = studentMapper.selectPage(new Page<>(page, size), wrapper);
        Page<StudentResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getCreatedAt());
    }
}
