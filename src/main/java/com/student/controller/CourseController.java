package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Course;
import com.student.service.CascadeService;
import com.student.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CascadeService cascadeService;

    @GetMapping
    public Result<Page<Course>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minCredit,
            @RequestParam(required = false) Integer maxCredit) {

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Course::getName, name);
        }
        if (minCredit != null) {
            wrapper.ge(Course::getCredit, minCredit);
        }
        if (maxCredit != null) {
            wrapper.le(Course::getCredit, maxCredit);
        }

        Page<Course> result = courseService.page(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<Course> add(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer credit = body.get("credit") != null ? ((Number) body.get("credit")).intValue() : null;

        if (name == null || name.isBlank()) return Result.error(400, "课程名称不能为空");
        if (credit == null || credit < 1 || credit > 10) return Result.error(400, "学分范围为1-10");

        Course c = new Course();
        c.setName(name);
        c.setCredit(credit);
        courseService.save(c);
        return Result.success(c);
    }

    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Course c = courseService.getById(id);
        if (c == null) return Result.error(404, "课程不存在");

        if (body.containsKey("name")) c.setName((String) body.get("name"));
        if (body.containsKey("credit")) {
            Integer credit = ((Number) body.get("credit")).intValue();
            if (credit < 1 || credit > 10) return Result.error(400, "学分范围为1-10");
            c.setCredit(credit);
        }
        courseService.updateById(c);
        return Result.success(c);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!courseService.removeById(id)) return Result.error(404, "课程不存在");
        cascadeService.deleteCourseCascade(id);
        return Result.success();
    }
}
