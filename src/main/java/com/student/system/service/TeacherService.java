package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.TeacherRequest;
import com.student.system.dto.TeacherResponse;
import com.student.system.entity.Grade;
import com.student.system.entity.Opening;
import com.student.system.entity.Selection;
import com.student.system.entity.Teacher;
import com.student.system.mapper.GradeMapper;
import com.student.system.mapper.OpeningMapper;
import com.student.system.mapper.SelectionMapper;
import com.student.system.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private OpeningMapper openingMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setName(request.getName());
        String pwd = (request.getPassword() != null && !request.getPassword().isEmpty())
                ? request.getPassword() : "123456";
        teacher.setPassword(passwordEncoder.encode(pwd));
        teacherMapper.insert(teacher);
        return toResponse(teacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new RuntimeException("教师不存在");
        }
        if (request.getName() != null) {
            teacher.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        teacherMapper.updateById(teacher);
        return toResponse(teacher);
    }

    @Transactional
    public void delete(Long id) {
        // Find all openings by this teacher
        List<Opening> openings = openingMapper.selectList(
                new QueryWrapper<Opening>().eq("teacher_id", id));
        for (Opening opening : openings) {
            // Delete selections for each opening, and their grades
            List<Selection> selections = selectionMapper.selectList(
                    new QueryWrapper<Selection>().eq("opening_id", opening.getId()));
            for (Selection sel : selections) {
                gradeMapper.delete(new QueryWrapper<Grade>().eq("selection_id", sel.getId()));
            }
            selectionMapper.delete(new QueryWrapper<Selection>().eq("opening_id", opening.getId()));
        }
        openingMapper.delete(new QueryWrapper<Opening>().eq("teacher_id", id));
        teacherMapper.deleteById(id);
    }

    public Page<TeacherResponse> search(String id, String name, boolean fuzzy, int page, int size) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
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
        Page<Teacher> result = teacherMapper.selectPage(new Page<>(page, size), wrapper);
        Page<TeacherResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    private TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(teacher.getId(), teacher.getName(), teacher.getCreatedAt());
    }
}
