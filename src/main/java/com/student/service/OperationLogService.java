package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.OperationLog;

import java.util.List;
import java.util.Map;

public interface OperationLogService extends IService<OperationLog> {
    void log(String operation, String operator, String operatorRole,
             String targetType, Long targetId, String detail);

    List<Map<String, Object>> queryLogs(String operator, String operation,
                                         String targetType, String startTime, String endTime);
}
