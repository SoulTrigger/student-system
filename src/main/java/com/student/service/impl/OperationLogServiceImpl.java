package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.OperationLog;
import com.student.mapper.OperationLogMapper;
import com.student.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void log(String operation, String operator, String operatorRole,
                    String targetType, Long targetId, String detail) {
        OperationLog ol = new OperationLog();
        ol.setOperation(operation);
        ol.setOperator(operator);
        ol.setOperatorRole(operatorRole);
        ol.setTargetType(targetType);
        ol.setTargetId(targetId);
        ol.setDetail(detail);
        ol.setCreatedAt(LocalDateTime.now());
        save(ol);
    }

    @Override
    public List<Map<String, Object>> queryLogs(String operator, String operation,
                                                String targetType, String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        if (operator != null && !operator.isBlank()) {
            wrapper.like(OperationLog::getOperator, operator);
        }
        if (operation != null && !operation.isBlank()) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(OperationLog::getTargetType, targetType);
        }
        if (startTime != null && !startTime.isBlank()) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(startTime, FMT));
        }
        if (endTime != null && !endTime.isBlank()) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(endTime, FMT));
        }
        return list(wrapper).stream().map(ol -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", ol.getId());
            map.put("operation", ol.getOperation());
            map.put("operator", ol.getOperator());
            map.put("operatorRole", ol.getOperatorRole());
            map.put("targetType", ol.getTargetType());
            map.put("targetId", ol.getTargetId());
            map.put("detail", ol.getDetail());
            map.put("createdAt", ol.getCreatedAt() != null ? ol.getCreatedAt().format(FMT) : null);
            return map;
        }).collect(Collectors.toList());
    }
}
