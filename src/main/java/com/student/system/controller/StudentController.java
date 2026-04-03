package com.student.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.system.context.RequestContextHolder;
import com.student.system.dto.StudentRequest;
import com.student.system.dto.StudentResponse;
import com.student.system.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    private ResponseEntity<?> checkAdmin() {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(studentService.list(page, size));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody StudentRequest request) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(studentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        studentService.delete(id);
        Map<String, String> body = new HashMap<>();
        body.put("message", "删除成功");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String id,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(defaultValue = "false") boolean fuzzy,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(studentService.search(id, name, fuzzy, page, size));
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
