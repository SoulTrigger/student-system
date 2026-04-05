package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.*;
import com.student.mapper.GradeMapper;
import com.student.service.CourseService;
import com.student.service.OpeningService;
import com.student.service.SelectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/selections")
@RequiredArgsConstructor
public class SelectionController {

    private final SelectionService selectionService;
    private final OpeningService openingService;
    private final CourseService courseService;
    private final GradeMapper gradeMapper;

    @GetMapping("/available")
    public Result<Page<Map<String, Object>>> available(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String semester) {

        LambdaQueryWrapper<Opening> wrapper = new LambdaQueryWrapper<>();
        if (semester != null && !semester.isBlank()) wrapper.eq(Opening::getSemester, semester);

        Page<Opening> openings = openingService.page(new Page<>(page, size), wrapper);
        Page<Map<String, Object>> result = new Page<>(openings.getCurrent(), openings.getSize(), openings.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Opening opn : openings.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("openingId", opn.getId());
            map.put("courseId", opn.getCourseId());
            map.put("teacherId", opn.getTeacherId());
            map.put("semester", opn.getSemester());
            Course c = courseService.getById(opn.getCourseId());
            if (c != null) {
                map.put("courseName", c.getName());
                map.put("credit", c.getCredit());
            }
            records.add(map);
        }
        result.setRecords(records);
        return Result.success(result);
    }

    @PostMapping
    public Result<Selection> select(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        Object oid = body.get("openingId");
        if (oid == null) return Result.error(400, "openingId不能为空");
        Long openingId = ((Number) oid).longValue();

        Opening opening = openingService.getById(openingId);
        if (opening == null) return Result.error(404, "开课记录不存在");

        long dupCount = selectionService.count(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getOpeningId, openingId)
                .eq(Selection::getStudentId, studentId));
        if (dupCount > 0) return Result.error(409, "已选过该课程");

        long currentCount = selectionService.count(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, studentId));
        if (currentCount >= 8) return Result.error(400, "选课数量已达上限(8门)");

        int currentCredits = calcCurrentCredits(studentId);
        Course course = courseService.getById(opening.getCourseId());
        if (course != null && currentCredits + course.getCredit() > 24) {
            return Result.error(400, "学分将超过上限(24)，当前已选" + currentCredits + "学分");
        }

        Selection sel = new Selection();
        sel.setOpeningId(openingId);
        sel.setStudentId(studentId);
        selectionService.save(sel);
        return Result.success(sel);
    }

    @GetMapping("/mine")
    public Result<Page<Map<String, Object>>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long studentId = (Long) request.getAttribute("userId");
        Page<Selection> selPage = selectionService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Selection>().eq(Selection::getStudentId, studentId));

        Page<Map<String, Object>> result = new Page<>(selPage.getCurrent(), selPage.getSize(), selPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Selection sel : selPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("selectionId", sel.getId());
            map.put("openingId", sel.getOpeningId());
            Opening opn = openingService.getById(sel.getOpeningId());
            if (opn != null) {
                map.put("courseId", opn.getCourseId());
                map.put("teacherId", opn.getTeacherId());
                map.put("semester", opn.getSemester());
                Course c = courseService.getById(opn.getCourseId());
                if (c != null) {
                    map.put("courseName", c.getName());
                    map.put("credit", c.getCredit());
                }
            }
            records.add(map);
        }
        result.setRecords(records);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> drop(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        Selection sel = selectionService.getById(id);
        if (sel == null) return Result.error(404, "选课记录不存在");
        if (!sel.getStudentId().equals(studentId)) return Result.error(403, "无权操作");

        gradeMapper.delete(new LambdaQueryWrapper<Grade>().eq(Grade::getSelectionId, id));
        selectionService.removeById(id);
        return Result.success();
    }

    @GetMapping("/credits")
    public Result<Map<String, Object>> totalCredits(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        int credits = calcCurrentCredits(studentId);
        long count = selectionService.count(new LambdaQueryWrapper<Selection>()
                .eq(Selection::getStudentId, studentId));
        Map<String, Object> map = new HashMap<>();
        map.put("totalCredits", credits);
        map.put("courseCount", count);
        map.put("maxCredits", 24);
        map.put("maxCourses", 8);
        return Result.success(map);
    }

    private int calcCurrentCredits(Long studentId) {
        List<Selection> selections = selectionService.list(
                new LambdaQueryWrapper<Selection>().eq(Selection::getStudentId, studentId));
        int total = 0;
        for (Selection sel : selections) {
            Opening opn = openingService.getById(sel.getOpeningId());
            if (opn != null) {
                Course c = courseService.getById(opn.getCourseId());
                if (c != null) total += c.getCredit();
            }
        }
        return total;
    }
}
