package com.student.controller;

import com.student.common.Result;
import com.student.entity.Grade;
import com.student.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/batch")
    public Result<List<Grade>> batchSave(@RequestBody List<Grade> grades, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"teacher".equals(role) && !"admin".equals(role)) {
            return Result.error(403, "无权操作");
        }
        try {
            return Result.success(gradeService.batchSave(grades));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/teacher")
    public Result<List<Map<String, Object>>> listByTeacher(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        Long teacherId = (Long) request.getAttribute("userId");
        return Result.success(gradeService.listByTeacher(teacherId, semester));
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权操作");
        }
        return Result.success(gradeService.listAll(semester));
    }

    @PutMapping("/{id}")
    public Result<Void> updateScore(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            return Result.error(403, "无权操作");
        }
        Object scoreObj = body.get("score");
        if (scoreObj == null) return Result.error(400, "score不能为空");
        int score = ((Number) scoreObj).intValue();
        try {
            gradeService.updateScore(id, score, role);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/student")
    public Result<List<Map<String, Object>>> listByStudent(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return Result.success(gradeService.listByStudent(studentId));
    }

    @GetMapping("/student/average")
    public Result<Map<String, Object>> studentAverage(HttpServletRequest request) {
        Long studentId = (Long) request.getAttribute("userId");
        return Result.success(gradeService.studentAverage(studentId));
    }
}
