package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Opening;
import com.student.service.CascadeService;
import com.student.service.OpeningService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/openings")
@RequiredArgsConstructor
public class OpeningController {

    private final OpeningService openingService;
    private final CascadeService cascadeService;

    @GetMapping
    public Result<Page<Opening>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String semester) {

        LambdaQueryWrapper<Opening> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) wrapper.eq(Opening::getCourseId, courseId);
        if (teacherId != null) wrapper.eq(Opening::getTeacherId, teacherId);
        if (semester != null && !semester.isBlank()) wrapper.like(Opening::getSemester, semester);

        return Result.success(openingService.page(new Page<>(page, size), wrapper));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (openingService.getById(id) == null) return Result.error(404, "开课记录不存在");
        cascadeService.deleteOpeningCascade(id);
        return Result.success();
    }

    @PostMapping
    public Result<Opening> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long teacherId;
        String role = (String) request.getAttribute("role");

        if ("TEACHER".equals(role)) {
            teacherId = (Long) request.getAttribute("userId");
        } else {
            Object tid = body.get("teacherId");
            if (tid == null) return Result.error(400, "teacherId不能为空");
            teacherId = ((Number) tid).longValue();
        }

        Object cid = body.get("courseId");
        if (cid == null) return Result.error(400, "courseId不能为空");
        Long courseId = ((Number) cid).longValue();

        String semester = (String) body.get("semester");
        if (semester == null || semester.isBlank()) return Result.error(400, "学期不能为空");

        boolean exists = openingService.count(new LambdaQueryWrapper<Opening>()
                .eq(Opening::getTeacherId, teacherId)
                .eq(Opening::getCourseId, courseId)
                .eq(Opening::getSemester, semester)) > 0;
        if (exists) return Result.error(409, "该教师已在本学期开设此课程");

        Opening opening = new Opening();
        opening.setCourseId(courseId);
        opening.setTeacherId(teacherId);
        opening.setSemester(semester);
        openingService.save(opening);
        return Result.success(opening);
    }

    @GetMapping("/mine")
    public Result<Page<Opening>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Long teacherId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<Opening> wrapper = new LambdaQueryWrapper<Opening>()
                .eq(Opening::getTeacherId, teacherId);
        return Result.success(openingService.page(new Page<>(page, size), wrapper));
    }
}
