package com.student.service;

import com.student.entity.OperationLog;
import com.student.mapper.OperationLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OperationLogServiceTest {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @BeforeEach
    void cleanup() {
        operationLogMapper.delete(null);
    }

    @Test
    void testLogCreatesRecord() {
        operationLogService.log("成绩录入", "admin", "ADMIN", "grade", 1L, "批量录入");
        List<OperationLog> logs = operationLogMapper.selectList(null);
        assertEquals(1, logs.size());
        OperationLog ol = logs.get(0);
        assertEquals("成绩录入", ol.getOperation());
        assertEquals("admin", ol.getOperator());
        assertEquals("ADMIN", ol.getOperatorRole());
        assertEquals("grade", ol.getTargetType());
        assertEquals(1L, ol.getTargetId());
        assertNotNull(ol.getCreatedAt());
    }

    @Test
    void testQueryLogsNoFilter() {
        operationLogService.log("选课", "stu1", "STUDENT", "selection", null, "选课");
        operationLogService.log("退课", "stu1", "STUDENT", "selection", 2L, "退课");
        List<Map<String, Object>> result = operationLogService.queryLogs(null, null, null, null, null);
        assertEquals(2, result.size());
    }

    @Test
    void testQueryLogsByOperator() {
        operationLogService.log("选课", "stu1", "STUDENT", "selection", null, "");
        operationLogService.log("退课", "stu2", "STUDENT", "selection", null, "");
        List<Map<String, Object>> result = operationLogService.queryLogs("stu1", null, null, null, null);
        assertEquals(1, result.size());
        assertEquals("stu1", result.get(0).get("operator"));
    }

    @Test
    void testQueryLogsByOperation() {
        operationLogService.log("成绩录入", "admin", "ADMIN", "grade", null, "");
        operationLogService.log("退课", "stu1", "STUDENT", "selection", null, "");
        List<Map<String, Object>> result = operationLogService.queryLogs(null, "成绩", null, null, null);
        assertEquals(1, result.size());
        assertTrue(result.get(0).get("operation").toString().contains("成绩"));
    }

    @Test
    void testQueryLogsByTargetType() {
        operationLogService.log("选课", "stu1", "STUDENT", "selection", null, "");
        operationLogService.log("成绩录入", "admin", "ADMIN", "grade", null, "");
        List<Map<String, Object>> result = operationLogService.queryLogs(null, null, "grade", null, null);
        assertEquals(1, result.size());
        assertEquals("grade", result.get(0).get("targetType"));
    }

    @Test
    void testQueryLogsByTimeRange() {
        operationLogService.log("old", "admin", "ADMIN", "grade", null, "");
        List<Map<String, Object>> result = operationLogService.queryLogs(
                null, null, null,
                "2020-01-01 00:00:00", "2099-12-31 23:59:59");
        assertFalse(result.isEmpty());
    }

    @Test
    void testQueryLogsEmptyResult() {
        List<Map<String, Object>> result = operationLogService.queryLogs(null, null, null, null, null);
        assertTrue(result.isEmpty());
    }
}
