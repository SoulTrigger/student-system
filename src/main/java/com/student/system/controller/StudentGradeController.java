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
@RequestMapping("/api/student/grades")
public class StudentGradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String semester,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        if (!"student".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long studentId = RequestContextHolder.getUserId();
        return ResponseEntity.ok(gradeService.studentGrades(studentId, semester, page, size));
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
