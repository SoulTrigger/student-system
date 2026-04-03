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
@RequestMapping("/api/teacher/grades")
public class TeacherGradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) String studentName,
                                  @RequestParam(required = false) Long courseId,
                                  @RequestParam(required = false) String courseName,
                                  @RequestParam(required = false) Integer scoreMin,
                                  @RequestParam(required = false) Integer scoreMax,
                                  @RequestParam(required = false) String semester,
                                  @RequestParam(defaultValue = "false") boolean fuzzy,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        return ResponseEntity.ok(gradeService.teacherGrades(teacherId, studentId, studentName,
                courseId, courseName, scoreMin, scoreMax, semester, fuzzy, page, size));
    }

    @PostMapping
    public ResponseEntity<?> enter(@RequestBody Map<String, Object> body) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        Long selectionId = ((Number) body.get("selectionId")).longValue();
        Integer score = ((Number) body.get("score")).intValue();
        try {
            return ResponseEntity.ok(gradeService.enterGrade(teacherId, selectionId, score));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        Integer score = ((Number) body.get("score")).intValue();
        try {
            return ResponseEntity.ok(gradeService.editGrade(teacherId, id, score));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> studentsInOpening(@RequestParam Long openingId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        try {
            return ResponseEntity.ok(gradeService.studentsInOpening(teacherId, openingId, page, size));
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
