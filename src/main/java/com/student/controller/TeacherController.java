package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Teacher;
import com.student.service.CascadeService;
import com.student.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final CascadeService cascadeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {

        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Teacher::getName, name);
        }
        wrapper.select(Teacher::getId, Teacher::getName, Teacher::getCreatedAt);

        Page<Teacher> teacherPage = teacherService.page(new Page<>(page, size), wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(teacherPage.getCurrent(), teacherPage.getSize(), teacherPage.getTotal());
        resultPage.setRecords(teacherPage.getRecords().stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("createdAt", t.getCreatedAt());
            return m;
        }).toList());
        return Result.success(resultPage);
    }

    @PostMapping
    public Result<Teacher> add(@RequestBody Map<String, String> body) {
        Teacher t = new Teacher();
        t.setName(body.get("name"));
        t.setPassword(passwordEncoder.encode(body.getOrDefault("password", "123456")));
        teacherService.save(t);
        t.setPassword(null);
        return Result.success(t);
    }

    @PutMapping("/{id}")
    public Result<Teacher> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Teacher t = teacherService.getById(id);
        if (t == null) return Result.error(404, "教师不存在");
        if (body.containsKey("name")) t.setName(body.get("name"));
        if (body.containsKey("password")) t.setPassword(passwordEncoder.encode(body.get("password")));
        teacherService.updateById(t);
        t.setPassword(null);
        return Result.success(t);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!teacherService.removeById(id)) return Result.error(404, "教师不存在");
        cascadeService.deleteTeacherCascade(id);
        return Result.success();
    }
}
