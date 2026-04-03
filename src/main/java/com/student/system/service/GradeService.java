package com.student.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.dto.GradeResponse;
import com.student.system.entity.*;
import com.student.system.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradeService {

    @Autowired
    private GradeMapper gradeMapper;
    @Autowired
    private SelectionMapper selectionMapper;
    @Autowired
    private OpeningMapper openingMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentMapper studentMapper;

    // --- Teacher APIs ---

    public Page<GradeResponse> teacherGrades(Long teacherId, Long studentId, String studentName,
                                              Long courseId, String courseName,
                                              Integer scoreMin, Integer scoreMax,
                                              String semester, boolean fuzzy,
                                              int page, int size) {
        // Get all openings for this teacher
        List<Opening> teacherOpenings = openingMapper.selectList(
                new QueryWrapper<Opening>().eq("teacher_id", teacherId));
        List<Long> openingIds = teacherOpenings.stream().map(Opening::getId).collect(Collectors.toList());

        if (openingIds.isEmpty()) {
            Page<GradeResponse> empty = new Page<>(page, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        // Get selections for those openings
        List<Selection> selections = selectionMapper.selectList(
                new QueryWrapper<Selection>().in("opening_id", openingIds));
        List<Long> selectionIds = selections.stream().map(Selection::getId).collect(Collectors.toList());

        if (selectionIds.isEmpty()) {
            Page<GradeResponse> empty = new Page<>(page, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        QueryWrapper<Grade> wrapper = new QueryWrapper<Grade>()
                .in("selection_id", selectionIds);

        if (semester != null && !semester.isEmpty()) {
            wrapper.eq("semester", semester);
        }
        if (scoreMin != null) {
            wrapper.ge("score", scoreMin);
        }
        if (scoreMax != null) {
            wrapper.le("score", scoreMax);
        }
        wrapper.orderByDesc("id");

        Page<Grade> gradePage = gradeMapper.selectPage(new Page<>(page, size), wrapper);

        Page<GradeResponse> responsePage = new Page<>(gradePage.getCurrent(), gradePage.getSize(), gradePage.getTotal());
        responsePage.setRecords(gradePage.getRecords().stream()
                .map(g -> toGradeResponse(g, selections, teacherOpenings))
                .filter(r -> filterGrade(r, studentId, studentName, courseId, courseName, fuzzy))
                .collect(Collectors.toList()));
        return responsePage;
    }

    @Transactional
    public GradeResponse enterGrade(Long teacherId, Long selectionId, Integer score) {
        validateScore(score);

        Selection selection = selectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new RuntimeException("选课记录不存在");
        }

        Opening opening = openingMapper.selectById(selection.getOpeningId());
        if (opening == null || !opening.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("只能为自己的课程录入成绩");
        }

        // Check if grade already exists
        Grade existing = gradeMapper.selectOne(
                new QueryWrapper<Grade>().eq("selection_id", selectionId));
        if (existing != null) {
            throw new RuntimeException("该学生已有成绩记录");
        }

        Grade grade = new Grade();
        grade.setSelectionId(selectionId);
        grade.setScore(score);
        grade.setSemester(opening.getSemester());
        gradeMapper.insert(grade);

        return toGradeResponse(grade, List.of(selection), List.of(opening));
    }

    @Transactional
    public GradeResponse editGrade(Long teacherId, Long gradeId, Integer score) {
        validateScore(score);

        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new RuntimeException("成绩记录不存在");
        }

        Selection selection = selectionMapper.selectById(grade.getSelectionId());
        Opening opening = openingMapper.selectById(selection.getOpeningId());
        if (!opening.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("只能编辑自己课程的成绩");
        }

        grade.setScore(score);
        gradeMapper.updateById(grade);
        return toGradeResponse(grade, List.of(selection), List.of(opening));
    }

    public Page<GradeResponse> studentsInOpening(Long teacherId, Long openingId, int page, int size) {
        Opening opening = openingMapper.selectById(openingId);
        if (opening == null || !opening.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("无权限查看该开课的学生");
        }

        QueryWrapper<Selection> selWrapper = new QueryWrapper<Selection>()
                .eq("opening_id", openingId);
        Page<Selection> selPage = selectionMapper.selectPage(new Page<>(page, size), selWrapper);

        Page<GradeResponse> responsePage = new Page<>(selPage.getCurrent(), selPage.getSize(), selPage.getTotal());
        responsePage.setRecords(selPage.getRecords().stream().map(sel -> {
            GradeResponse r = new GradeResponse();
            r.setSelectionId(sel.getId());
            r.setStudentId(sel.getStudentId());
            Student student = studentMapper.selectById(sel.getStudentId());
            if (student != null) r.setStudentName(student.getName());
            // Check if grade exists
            Grade g = gradeMapper.selectOne(new QueryWrapper<Grade>().eq("selection_id", sel.getId()));
            if (g != null) {
                r.setId(g.getId());
                r.setScore(g.getScore());
                r.setSemester(g.getSemester());
            }
            r.setCourseId(opening.getCourseId());
            Course c = courseMapper.selectById(opening.getCourseId());
            if (c != null) r.setCourseName(c.getName());
            r.setTeacherId(opening.getTeacherId());
            Teacher t = teacherMapper.selectById(opening.getTeacherId());
            if (t != null) r.setTeacherName(t.getName());
            return r;
        }).collect(Collectors.toList()));
        return responsePage;
    }

    // --- Admin APIs ---

    public Page<GradeResponse> adminGrades(Long studentId, String studentName,
                                            Long teacherId, String teacherName,
                                            Long courseId, String courseName,
                                            Integer scoreMin, Integer scoreMax,
                                            String semester, boolean fuzzy,
                                            int page, int size) {
        QueryWrapper<Grade> wrapper = new QueryWrapper<>();
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq("semester", semester);
        }
        if (scoreMin != null) {
            wrapper.ge("score", scoreMin);
        }
        if (scoreMax != null) {
            wrapper.le("score", scoreMax);
        }
        wrapper.orderByDesc("id");

        Page<Grade> gradePage = gradeMapper.selectPage(new Page<>(page, size), wrapper);

        List<Selection> allSelections = selectionMapper.selectList(null);
        List<Opening> allOpenings = openingMapper.selectList(null);

        Page<GradeResponse> responsePage = new Page<>(gradePage.getCurrent(), gradePage.getSize(), gradePage.getTotal());
        responsePage.setRecords(gradePage.getRecords().stream()
                .map(g -> toGradeResponse(g, allSelections, allOpenings))
                .filter(r -> filterGrade(r, studentId, studentName, courseId, courseName, fuzzy))
                .filter(r -> filterTeacher(r, teacherId, teacherName, fuzzy))
                .collect(Collectors.toList()));
        return responsePage;
    }

    @Transactional
    public GradeResponse adminEditGrade(Long gradeId, Integer score) {
        validateScore(score);
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new RuntimeException("成绩记录不存在");
        }
        grade.setScore(score);
        gradeMapper.updateById(grade);
        return toGradeResponse(grade, selectionMapper.selectList(null), openingMapper.selectList(null));
    }

    // --- Student API ---

    public Map<String, Object> studentGrades(Long studentId, String semester, int page, int size) {
        // Get all selections for this student
        List<Selection> studentSelections = selectionMapper.selectList(
                new QueryWrapper<Selection>().eq("student_id", studentId));
        List<Long> selIds = studentSelections.stream().map(Selection::getId).collect(Collectors.toList());

        if (selIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            Page<GradeResponse> empty = new Page<>(page, size, 0);
            empty.setRecords(List.of());
            result.put("grades", empty);
            result.put("averageScore", BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
            return result;
        }

        QueryWrapper<Grade> wrapper = new QueryWrapper<Grade>()
                .in("selection_id", selIds);
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq("semester", semester);
        }
        wrapper.orderByDesc("id");

        Page<Grade> gradePage = gradeMapper.selectPage(new Page<>(page, size), wrapper);

        List<Opening> allOpenings = openingMapper.selectList(null);

        List<GradeResponse> grades = gradePage.getRecords().stream()
                .map(g -> toGradeResponse(g, studentSelections, allOpenings))
                .collect(Collectors.toList());

        Page<GradeResponse> responsePage = new Page<>(gradePage.getCurrent(), gradePage.getSize(), gradePage.getTotal());
        responsePage.setRecords(grades);

        // Calculate average score (all grades for this student, not just current page)
        QueryWrapper<Grade> avgWrapper = new QueryWrapper<Grade>()
                .in("selection_id", selIds)
                .isNotNull("score");
        if (semester != null && !semester.isEmpty()) {
            avgWrapper.eq("semester", semester);
        }
        List<Grade> allGrades = gradeMapper.selectList(avgWrapper);
        BigDecimal avg = BigDecimal.ZERO;
        if (!allGrades.isEmpty()) {
            int sum = 0;
            for (Grade g : allGrades) {
                if (g.getScore() != null) sum += g.getScore();
            }
            avg = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(allGrades.size()), 1, RoundingMode.HALF_UP);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("grades", responsePage);
        result.put("averageScore", avg);
        return result;
    }

    // --- Helpers ---

    private void validateScore(Integer score) {
        if (score == null || score < 0 || score > 100) {
            throw new RuntimeException("成绩必须在0-100之间");
        }
    }

    private GradeResponse toGradeResponse(Grade grade, List<Selection> selections, List<Opening> openings) {
        GradeResponse r = new GradeResponse();
        r.setId(grade.getId());
        r.setSelectionId(grade.getSelectionId());
        r.setScore(grade.getScore());
        r.setSemester(grade.getSemester());

        Selection sel = selections.stream()
                .filter(s -> s.getId().equals(grade.getSelectionId()))
                .findFirst().orElse(null);
        if (sel != null) {
            r.setStudentId(sel.getStudentId());
            Student student = studentMapper.selectById(sel.getStudentId());
            if (student != null) r.setStudentName(student.getName());

            Opening opening = openings.stream()
                    .filter(o -> o.getId().equals(sel.getOpeningId()))
                    .findFirst().orElse(null);
            if (opening != null) {
                r.setTeacherId(opening.getTeacherId());
                Teacher teacher = teacherMapper.selectById(opening.getTeacherId());
                if (teacher != null) r.setTeacherName(teacher.getName());
                r.setCourseId(opening.getCourseId());
                Course course = courseMapper.selectById(opening.getCourseId());
                if (course != null) r.setCourseName(course.getName());
            }
        }
        return r;
    }

    private boolean filterGrade(GradeResponse r, Long studentId, String studentName,
                                Long courseId, String courseName, boolean fuzzy) {
        if (studentId != null && !studentId.equals(r.getStudentId())) return false;
        if (studentName != null && !studentName.isEmpty()) {
            if (r.getStudentName() == null) return false;
            if (fuzzy) {
                if (!r.getStudentName().contains(studentName)) return false;
            } else {
                if (!studentName.equals(r.getStudentName())) return false;
            }
        }
        if (courseId != null && !courseId.equals(r.getCourseId())) return false;
        if (courseName != null && !courseName.isEmpty()) {
            if (r.getCourseName() == null) return false;
            if (fuzzy) {
                if (!r.getCourseName().contains(courseName)) return false;
            } else {
                if (!courseName.equals(r.getCourseName())) return false;
            }
        }
        return true;
    }

    private boolean filterTeacher(GradeResponse r, Long teacherId, String teacherName, boolean fuzzy) {
        if (teacherId != null && !teacherId.equals(r.getTeacherId())) return false;
        if (teacherName != null && !teacherName.isEmpty()) {
            if (r.getTeacherName() == null) return false;
            if (fuzzy) {
                if (!r.getTeacherName().contains(teacherName)) return false;
            } else {
                if (!teacherName.equals(r.getTeacherName())) return false;
            }
        }
        return true;
    }
}
