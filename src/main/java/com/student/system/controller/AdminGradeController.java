package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/grades")
public class AdminGradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) String studentName,
                                  @RequestParam(required = false) Long teacherId,
                                  @RequestParam(required = false) String teacherName,
                                  @RequestParam(required = false) Long courseId,
                                  @RequestParam(required = false) String courseName,
                                  @RequestParam(required = false) Integer scoreMin,
                                  @RequestParam(required = false) Integer scoreMax,
                                  @RequestParam(required = false) String semester,
                                  @RequestParam(defaultValue = "false") boolean fuzzy,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        return ResponseEntity.ok(gradeService.adminGrades(studentId, studentName, teacherId,
                teacherName, courseId, courseName, scoreMin, scoreMax, semester, fuzzy, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Integer score = ((Number) body.get("score")).intValue();
        try {
            return ResponseEntity.ok(gradeService.adminEditGrade(id, score));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
