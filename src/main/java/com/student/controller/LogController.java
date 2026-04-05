package com.student.controller;

import com.student.common.Result;
import com.student.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权操作");
        }
        return Result.success(operationLogService.queryLogs(operator, operation, targetType, startTime, endTime));
    }
}
