package com.student.system.controller;

import com.student.system.context.RequestContextHolder;
import com.student.system.service.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student/selections")
public class StudentSelectionController {

    @Autowired
    private SelectionService selectionService;

    @GetMapping("/available")
    public ResponseEntity<?> listAvailable(
            @RequestParam(required = false) String courseName,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!"student".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long studentId = RequestContextHolder.getUserId();
        return ResponseEntity.ok(selectionService.listAvailable(studentId, courseName, fuzzy, page, size));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Long> body) {
        if (!"student".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long studentId = RequestContextHolder.getUserId();
        Long openingId = body.get("openingId");
        if (openingId == null) {
            return ResponseEntity.badRequest().body(errorBody("openingId不能为空"));
        }
        try {
            return ResponseEntity.ok(selectionService.create(studentId, openingId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> listMine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!"student".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long studentId = RequestContextHolder.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("selections", selectionService.listMine(studentId, page, size));
        result.put("totalCredits", selectionService.getTotalCredits(studentId));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdraw(@PathVariable Long id) {
        if (!"student".equals(RequestContextHolder.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody("无权限"));
        }
        Long studentId = RequestContextHolder.getUserId();
        try {
            selectionService.withdraw(studentId, id);
            Map<String, String> body = new HashMap<>();
            body.put("message", "退课成功");
            return ResponseEntity.ok(body);
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
