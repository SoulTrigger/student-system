package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.OpeningResponse;
import com.student.system.entity.*;
import com.student.system.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpeningService {

    @Autowired
    private OpeningMapper openingMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private GradeMapper gradeMapper;

    public Page<OpeningResponse> search(Long teacherId, String teacherName, Long courseId,
                                         String courseName, boolean fuzzy, int page, int size) {
        QueryWrapper<Opening> wrapper = new QueryWrapper<>();

        if (teacherId != null) {
            wrapper.eq("teacher_id", teacherId);
        }
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }

        // For name-based filters, we need to join logic manually
        // Get all openings matching ID filters first, then filter by names
        wrapper.orderByDesc("id");
        Page<Opening> result = openingMapper.selectPage(new Page<>(page, size), wrapper);

        List<OpeningResponse> filtered = result.getRecords().stream()
                .map(this::toResponse)
                .filter(r -> {
                    if (teacherName != null && !teacherName.isEmpty()) {
                        if (fuzzy) {
                            if (r.getTeacherName() == null || !r.getTeacherName().contains(teacherName)) return false;
                        } else {
                            if (!teacherName.equals(r.getTeacherName())) return false;
                        }
                    }
                    if (courseName != null && !courseName.isEmpty()) {
                        if (fuzzy) {
                            if (r.getCourseName() == null || !r.getCourseName().contains(courseName)) return false;
                        } else {
                            if (!courseName.equals(r.getCourseName())) return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Page<OpeningResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(filtered);
        return responsePage;
    }

    @Transactional
    public void delete(Long id) {
        // Cascade: delete grades via selections, then selections, then opening
        List<Selection> selections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("opening_id", id));
        for (Selection sel : selections) {
            gradeMapper.delete(new QueryWrapper<Grade>().eq("selection_id", sel.getId()));
        }
        selectionMapper.delete(new QueryWrapper<Selection>().eq("opening_id", id));
        openingMapper.deleteById(id);
    }

    public Page<OpeningResponse> listByTeacher(Long teacherId, int page, int size) {
        QueryWrapper<Opening> wrapper = new QueryWrapper<Opening>()
                .eq("teacher_id", teacherId)
                .orderByDesc("id");
        Page<Opening> result = openingMapper.selectPage(new Page<>(page, size), wrapper);
        Page<OpeningResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).collect(Collectors.toList()));
        return responsePage;
    }

    @Transactional
    public OpeningResponse createForTeacher(Long teacherId, Long courseId) {
        String semester = getCurrentSemester();

        // Check course exists
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // Prevent duplicate
        Long count = openingMapper.selectCount(new QueryWrapper<Opening>()
                .eq("teacher_id", teacherId)
                .eq("course_id", courseId)
                .eq("semester", semester));
        if (count > 0) {
            throw new RuntimeException("本学期已开过该课程");
        }

        Opening opening = new Opening();
        opening.setTeacherId(teacherId);
        opening.setCourseId(courseId);
        opening.setSemester(semester);
        openingMapper.insert(opening);

        Teacher teacher = teacherMapper.selectById(teacherId);
        return new OpeningResponse(opening.getId(), courseId, course.getName(),
                teacherId, teacher != null ? teacher.getName() : null, semester);
    }

    public List<Course> getAvailableCourses(Long teacherId) {
        String semester = getCurrentSemester();
        // Get course IDs already opened by this teacher in current semester
        List<Opening> openings = openingMapper.selectList(new QueryWrapper<Opening>()
                .eq("teacher_id", teacherId)
                .eq("semester", semester));
        List<Long> openedCourseIds = openings.stream().map(Opening::getCourseId).collect(Collectors.toList());

        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        if (!openedCourseIds.isEmpty()) {
            wrapper.notIn("id", openedCourseIds);
        }
        return courseMapper.selectList(wrapper);
    }

    public static String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        // 上学期: Aug-Jan, 下学期: Feb-Jul
        int month = now.getMonthValue();
        if (month >= 8) {
            return year + "上学期";
        } else {
            return year + "下学期";
        }
    }

    private OpeningResponse toResponse(Opening opening) {
        String courseName = null;
        String teacherName = null;
        if (opening.getCourseId() != null) {
            Course course = courseMapper.selectById(opening.getCourseId());
            if (course != null) courseName = course.getName();
        }
        if (opening.getTeacherId() != null) {
            Teacher teacher = teacherMapper.selectById(opening.getTeacherId());
            if (teacher != null) teacherName = teacher.getName();
        }
        return new OpeningResponse(opening.getId(), opening.getCourseId(), courseName,
                opening.getTeacherId(), teacherName, opening.getSemester());
    }
}
