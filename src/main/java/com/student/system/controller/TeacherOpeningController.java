package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.entity.Course;
import com.student.system.service.OpeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherOpeningController {

    @Autowired
    private OpeningService openingService;

    @GetMapping("/openings")
    public ResponseEntity<?> listOpenings(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        return ResponseEntity.ok(openingService.listByTeacher(teacherId, page, size));
    }

    @PostMapping("/openings")
    public ResponseEntity<?> createOpening(@RequestBody Map<String, Long> body) {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long courseId = body.get("courseId");
        if (courseId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "courseId不能为空");
            return ResponseEntity.badRequest().body(err);
        }
        Long teacherId = RequestContextHolder.getUserId();
        try {
            return ResponseEntity.ok(openingService.createForTeacher(teacherId, courseId));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/courses/available")
    public ResponseEntity<?> availableCourses() {
        if (!"teacher".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long teacherId = RequestContextHolder.getUserId();
        List<Course> courses = openingService.getAvailableCourses(teacherId);
        return ResponseEntity.ok(courses);
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
