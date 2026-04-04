package com.student.service;

public interface CascadeService {
    void deleteStudentCascade(Long studentId);
    void deleteTeacherCascade(Long teacherId);
    void deleteCourseCascade(Long courseId);
    void deleteOpeningCascade(Long openingId);
}
