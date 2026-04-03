package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.AvailableOpeningResponse;
import com.student.system.dto.SelectionResponse;
import com.student.system.entity.*;
import com.student.system.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SelectionService {

    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private OpeningMapper openingMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private GradeMapper gradeMapper;

    public Page<AvailableOpeningResponse> listAvailable(Long studentId, String courseName,
                                                         boolean fuzzy, int page, int size) {
        // Get opening IDs already selected by this student
        List<Selection> mySelections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("student_id", studentId));
        List<Long> selectedOpeningIds = mySelections.stream()
                .map(Selection::getOpeningId).collect(Collectors.toList());

        QueryWrapper<Opening> wrapper = new QueryWrapper<>();
        if (!selectedOpeningIds.isEmpty()) {
            wrapper.notIn("id", selectedOpeningIds);
        }
        wrapper.orderByDesc("id");
        Page<Opening> result = openingMapper.selectPage(new Page<>(page, size), wrapper);

        List<AvailableOpeningResponse> filtered = result.getRecords().stream()
                .map(this::toAvailableResponse)
                .filter(r -> {
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

        Page<AvailableOpeningResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(filtered);
        return responsePage;
    }

    @Transactional
    public SelectionResponse create(Long studentId, Long openingId) {
        // Check opening exists
        Opening opening = openingMapper.selectById(openingId);
        if (opening == null) {
            throw new RuntimeException("开课记录不存在");
        }

        // Check not already selected
        Long count = selectionMapper.selectCount(new QueryWrapper<Selection>()
                .eq("student_id", studentId)
                .eq("opening_id", openingId));
        if (count > 0) {
            throw new RuntimeException("已经选过该课程");
        }

        // Get current selections for credit/course limit check
        List<Selection> currentSelections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("student_id", studentId));

        // Check 8 course limit
        if (currentSelections.size() >= 8) {
            throw new RuntimeException("每学期最多选8门课");
        }

        // Check 24 credit limit
        int totalCredits = 0;
        for (Selection sel : currentSelections) {
            Opening op = openingMapper.selectById(sel.getOpeningId());
            if (op != null) {
                Course c = courseMapper.selectById(op.getCourseId());
                if (c != null && c.getCredit() != null) {
                    totalCredits += c.getCredit();
                }
            }
        }
        Course newCourse = courseMapper.selectById(opening.getCourseId());
        int newCredit = (newCourse != null && newCourse.getCredit() != null) ? newCourse.getCredit() : 0;
        if (totalCredits + newCredit > 24) {
            throw new RuntimeException("总学分不能超过24");
        }

        Selection selection = new Selection();
        selection.setStudentId(studentId);
        selection.setOpeningId(openingId);
        selectionMapper.insert(selection);

        return toSelectionResponse(selection);
    }

    public Page<SelectionResponse> listMine(Long studentId, int page, int size) {
        QueryWrapper<Selection> wrapper = new QueryWrapper<Selection>()
                .eq("student_id", studentId)
                .orderByDesc("id");
        Page<Selection> result = selectionMapper.selectPage(new Page<>(page, size), wrapper);
        Page<SelectionResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::toSelectionResponse).collect(Collectors.toList()));
        return responsePage;
    }

    public int getTotalCredits(Long studentId) {
        List<Selection> selections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("student_id", studentId));
        int total = 0;
        for (Selection sel : selections) {
            Opening op = openingMapper.selectById(sel.getOpeningId());
            if (op != null) {
                Course c = courseMapper.selectById(op.getCourseId());
                if (c != null && c.getCredit() != null) {
                    total += c.getCredit();
                }
            }
        }
        return total;
    }

    @Transactional
    public void withdraw(Long studentId, Long selectionId) {
        Selection selection = selectionMapper.selectById(selectionId);
        if (selection == null || !selection.getStudentId().equals(studentId)) {
            throw new RuntimeException("选课记录不存在");
        }
        // Delete associated grade
        gradeMapper.delete(new QueryWrapper<Grade>().eq("selection_id", selectionId));
        selectionMapper.deleteById(selectionId);
    }

    private AvailableOpeningResponse toAvailableResponse(Opening opening) {
        String courseName = null;
        Integer credit = null;
        String teacherName = null;
        if (opening.getCourseId() != null) {
            Course course = courseMapper.selectById(opening.getCourseId());
            if (course != null) {
                courseName = course.getName();
                credit = course.getCredit();
            }
        }
        if (opening.getTeacherId() != null) {
            Teacher teacher = teacherMapper.selectById(opening.getTeacherId());
            if (teacher != null) teacherName = teacher.getName();
        }
        return new AvailableOpeningResponse(opening.getId(), opening.getCourseId(), courseName, credit,
                opening.getTeacherId(), teacherName, opening.getSemester());
    }

    private SelectionResponse toSelectionResponse(Selection selection) {
        Opening opening = openingMapper.selectById(selection.getOpeningId());
        String courseName = null;
        Integer credit = null;
        String teacherName = null;
        Long courseId = null;
        Long teacherId = null;
        String semester = null;
        if (opening != null) {
            courseId = opening.getCourseId();
            teacherId = opening.getTeacherId();
            semester = opening.getSemester();
            if (opening.getCourseId() != null) {
                Course course = courseMapper.selectById(opening.getCourseId());
                if (course != null) {
                    courseName = course.getName();
                    credit = course.getCredit();
                }
            }
            if (opening.getTeacherId() != null) {
                Teacher teacher = teacherMapper.selectById(opening.getTeacherId());
                if (teacher != null) teacherName = teacher.getName();
            }
        }
        return new SelectionResponse(selection.getId(), selection.getOpeningId(), courseId, courseName,
                credit, teacherId, teacherName, semester);
    }
}
