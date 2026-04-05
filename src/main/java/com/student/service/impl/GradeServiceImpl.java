package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.*;
import com.student.mapper.GradeMapper;
import com.student.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    private final SelectionService selectionService;
    private final OpeningService openingService;
    private final CourseService courseService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    @Override
    @Transactional
    public List<Grade> batchSave(List<Grade> grades) {
        for (Grade g : grades) {
            if (g.getScore() == null || g.getScore() < 0 || g.getScore() > 100) {
                throw new IllegalArgumentException("分数必须在0-100之间");
            }
            // Validate selection exists
            Selection sel = selectionService.getById(g.getSelectionId());
            if (sel == null) {
                throw new IllegalArgumentException("选课记录不存在: " + g.getSelectionId());
            }
        }
        saveBatch(grades);
        return grades;
    }

    @Override
    public List<Map<String, Object>> listByTeacher(Long teacherId, String semester) {
        // Find openings by this teacher
        LambdaQueryWrapper<Opening> opnWrapper = new LambdaQueryWrapper<Opening>()
                .eq(Opening::getTeacherId, teacherId);
        if (semester != null && !semester.isBlank()) {
            opnWrapper.eq(Opening::getSemester, semester);
        }
        List<Opening> openings = openingService.list(opnWrapper);
        Set<Long> openingIds = openings.stream().map(Opening::getId).collect(Collectors.toSet());
        if (openingIds.isEmpty()) return Collections.emptyList();

        // Find selections for these openings
        List<Selection> selections = selectionService.list(
                new LambdaQueryWrapper<Selection>().in(Selection::getOpeningId, openingIds));
        Map<Long, Long> selToOpening = selections.stream()
                .collect(Collectors.toMap(Selection::getId, Selection::getOpeningId));
        Map<Long, Long> selToStudent = selections.stream()
                .collect(Collectors.toMap(Selection::getId, Selection::getStudentId));
        if (selToOpening.isEmpty()) return Collections.emptyList();

        // Find grades for these selections
        List<Grade> grades = list(new LambdaQueryWrapper<Grade>()
                .in(Grade::getSelectionId, selToOpening.keySet()));

        // Build opening info map
        Map<Long, Opening> opnMap = openings.stream()
                .collect(Collectors.toMap(Opening::getId, o -> o));
        // Course info
        Map<Long, Course> courseMap = new HashMap<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Grade g : grades) {
            Map<String, Object> map = new HashMap<>();
            map.put("gradeId", g.getId());
            map.put("selectionId", g.getSelectionId());
            map.put("score", g.getScore());
            map.put("semester", g.getSemester());

            Long opnId = selToOpening.get(g.getSelectionId());
            if (opnId != null) {
                Opening opn = opnMap.get(opnId);
                if (opn != null) {
                    map.put("courseId", opn.getCourseId());
                    Course c = courseMap.computeIfAbsent(opn.getCourseId(),
                            id -> courseService.getById(id));
                    if (c != null) {
                        map.put("courseName", c.getName());
                        map.put("credit", c.getCredit());
                    }
                }
            }
            Long stuId = selToStudent.get(g.getSelectionId());
            if (stuId != null) {
                Student stu = studentService.getById(stuId);
                if (stu != null) map.put("studentName", stu.getName());
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listAll(String semester) {
        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<>();
        if (semester != null && !semester.isBlank()) {
            wrapper.eq(Grade::getSemester, semester);
        }
        List<Grade> grades = list(wrapper);
        return buildGradeList(grades);
    }

    @Override
    public void updateScore(Long gradeId, Integer score, String role) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间");
        }
        Grade g = getById(gradeId);
        if (g == null) throw new IllegalArgumentException("成绩记录不存在");
        g.setScore(score);
        updateById(g);
    }

    @Override
    public List<Map<String, Object>> listByStudent(Long studentId) {
        List<Selection> selections = selectionService.list(
                new LambdaQueryWrapper<Selection>().eq(Selection::getStudentId, studentId));
        if (selections.isEmpty()) return Collections.emptyList();

        Set<Long> selIds = selections.stream().map(Selection::getId).collect(Collectors.toSet());
        List<Grade> grades = list(new LambdaQueryWrapper<Grade>().in(Grade::getSelectionId, selIds));
        return buildGradeList(grades);
    }

    @Override
    public Map<String, Object> studentAverage(Long studentId) {
        List<Map<String, Object>> grades = listByStudent(studentId);
        List<Integer> scores = grades.stream()
                .filter(g -> g.get("score") != null)
                .map(g -> (Integer) g.get("score"))
                .collect(Collectors.toList());
        double avg = 0.0;
        if (!scores.isEmpty()) {
            avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            avg = Math.round(avg * 10.0) / 10.0;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("averageScore", avg);
        result.put("courseCount", scores.size());
        return result;
    }

    private List<Map<String, Object>> buildGradeList(List<Grade> grades) {
        // Preload selections
        Set<Long> selIds = grades.stream().map(Grade::getSelectionId).collect(Collectors.toSet());
        List<Selection> selections = selIds.isEmpty() ? Collections.emptyList() :
                selectionService.listByIds(selIds);
        Map<Long, Selection> selMap = selections.stream()
                .collect(Collectors.toMap(Selection::getId, s -> s));
        Map<Long, Opening> opnCache = new HashMap<>();
        Map<Long, Course> courseCache = new HashMap<>();
        Map<Long, Student> stuCache = new HashMap<>();
        Map<Long, Teacher> tchCache = new HashMap<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Grade g : grades) {
            Map<String, Object> map = new HashMap<>();
            map.put("gradeId", g.getId());
            map.put("selectionId", g.getSelectionId());
            map.put("score", g.getScore());
            map.put("semester", g.getSemester());

            Selection sel = selMap.get(g.getSelectionId());
            if (sel != null) {
                Opening opn = opnCache.computeIfAbsent(sel.getOpeningId(),
                        id -> openingService.getById(id));
                if (opn != null) {
                    map.put("courseId", opn.getCourseId());
                    Course c = courseCache.computeIfAbsent(opn.getCourseId(),
                            id -> courseService.getById(id));
                    if (c != null) {
                        map.put("courseName", c.getName());
                        map.put("credit", c.getCredit());
                    }
                    Teacher t = tchCache.computeIfAbsent(opn.getTeacherId(),
                            id -> teacherService.getById(id));
                    if (t != null) map.put("teacherName", t.getName());
                }
                Student stu = stuCache.computeIfAbsent(sel.getStudentId(),
                        id -> studentService.getById(id));
                if (stu != null) map.put("studentName", stu.getName());
            }
            result.add(map);
        }
        return result;
    }
}
