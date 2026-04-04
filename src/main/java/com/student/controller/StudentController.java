package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Student;
import com.student.service.CascadeService;
import com.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final CascadeService cascadeService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Student::getName, name);
        }
        wrapper.select(Student::getId, Student::getName, Student::getCreatedAt);

        Page<Student> studentPage = studentService.page(new Page<>(page, size), wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(studentPage.getCurrent(), studentPage.getSize(), studentPage.getTotal());
        resultPage.setRecords(studentPage.getRecords().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("createdAt", s.getCreatedAt());
            return m;
        }).toList());
        return Result.success(resultPage);
    }

    @PostMapping
    public Result<Student> add(@RequestBody Map<String, String> body) {
        Student s = new Student();
        s.setName(body.get("name"));
        s.setPassword(passwordEncoder.encode(body.getOrDefault("password", "123456")));
        studentService.save(s);
        s.setPassword(null);
        return Result.success(s);
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Student s = studentService.getById(id);
        if (s == null) return Result.error(404, "学生不存在");
        if (body.containsKey("name")) s.setName(body.get("name"));
        if (body.containsKey("password")) s.setPassword(passwordEncoder.encode(body.get("password")));
        studentService.updateById(s);
        s.setPassword(null);
        return Result.success(s);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!studentService.removeById(id)) return Result.error(404, "学生不存在");
        cascadeService.deleteStudentCascade(id);
        return Result.success();
    }
}
