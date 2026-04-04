package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Grade;

import java.util.List;
import java.util.Map;

public interface GradeService extends IService<Grade> {

    List<Grade> batchSave(List<Grade> grades);

    List<Map<String, Object>> listByTeacher(Long teacherId, String semester);

    List<Map<String, Object>> listAll(String semester);

    void updateScore(Long gradeId, Integer score, String role);

    List<Map<String, Object>> listByStudent(Long studentId);

    Map<String, Object> studentAverage(Long studentId);
}
