package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.dto.TeacherRequest;
import com.student.system.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    private ResponseEntity<?> checkAdmin() {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherRequest request) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(teacherService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ResponseEntity<?> check = checkAdmin();
        if (check != null) return check;
        teacherService.delete(id);
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
        return ResponseEntity.ok(teacherService.search(id, name, fuzzy, page, size));
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
