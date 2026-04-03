package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.service.OpeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/openings")
public class AdminOpeningController {

    @Autowired
    private OpeningService openingService;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) Long teacherId,
                                    @RequestParam(required = false) String teacherName,
                                    @RequestParam(required = false) Long courseId,
                                    @RequestParam(required = false) String courseName,
                                    @RequestParam(defaultValue = "false") boolean fuzzy,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        return ResponseEntity.ok(openingService.search(teacherId, teacherName, courseId, courseName, fuzzy, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!"admin".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        openingService.delete(id);
        Map<String, String> body = new HashMap<>();
        body.put("message", "删除成功");
        return ResponseEntity.ok(body);
    }

    private Map<String, String> errorBody(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return body;
    }
}
