package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.CourseRequest;
import com.student.system.dto.CourseResponse;
import com.student.system.entity.Course;
import com.student.system.entity.Grade;
import com.student.system.entity.Opening;
import com.student.system.entity.Selection;
import com.student.system.mapper.CourseMapper;
import com.student.system.mapper.GradeMapper;
import com.student.system.mapper.OpeningMapper;
import com.student.system.mapper.SelectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private OpeningMapper openingMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private GradeMapper gradeMapper;

    public Page<CourseResponse> list(int page, int size) {
        Page<Course> result = courseMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<Course>().orderByDesc("id"));
        Page<CourseResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setCredit(request.getCredit());
        courseMapper.insert(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getCredit() != null) {
            course.setCredit(request.getCredit());
        }
        courseMapper.updateById(course);
        return toResponse(course);
    }

    @Transactional
    public void delete(Long id) {
        // Cascade: delete grades via selections linked to openings of this course
        List<Opening> openings = openingMapper.selectList(
                new QueryWrapper<Opening>().eq("course_id", id));
        for (Opening opening : openings) {
            List<Selection> selections = selectionMapper.selectList(
                    new QueryWrapper<Selection>().eq("opening_id", opening.getId()));
            for (Selection sel : selections) {
                gradeMapper.delete(new QueryWrapper<Grade>().eq("selection_id", sel.getId()));
            }
            selectionMapper.delete(new QueryWrapper<Selection>().eq("opening_id", opening.getId()));
        }
        openingMapper.delete(new QueryWrapper<Opening>().eq("course_id", id));
        courseMapper.deleteById(id);
    }

    public Page<CourseResponse> search(String id, String name, Integer creditMin, Integer creditMax,
                                        boolean fuzzy, int page, int size) {
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
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
        if (creditMin != null) {
            wrapper.ge("credit", creditMin);
        }
        if (creditMax != null) {
            wrapper.le("credit", creditMax);
        }
        wrapper.orderByDesc("id");
        Page<Course> result = courseMapper.selectPage(new Page<>(page, size), wrapper);
        Page<CourseResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getName(), course.getCredit(), (java.time.LocalDateTime) null);
    }
}
