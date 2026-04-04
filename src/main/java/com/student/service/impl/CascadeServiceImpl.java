package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.*;
import com.student.mapper.*;
import com.student.service.CascadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CascadeServiceImpl implements CascadeService {

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;
    private final OpeningMapper openingMapper;
    private final SelectionMapper selectionMapper;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public void deleteStudentCascade(Long studentId) {
        List<Selection> selections = selectionMapper.selectList(
                new LambdaQueryWrapper<Selection>().eq(Selection::getStudentId, studentId));
        for (Selection sel : selections) {
            gradeMapper.delete(new LambdaQueryWrapper<Grade>().eq(Grade::getSelectionId, sel.getId()));
        }
        selectionMapper.delete(new LambdaQueryWrapper<Selection>().eq(Selection::getStudentId, studentId));
        studentMapper.deleteById(studentId);
    }

    @Override
    @Transactional
    public void deleteTeacherCascade(Long teacherId) {
        List<Opening> openings = openingMapper.selectList(
                new LambdaQueryWrapper<Opening>().eq(Opening::getTeacherId, teacherId));
        for (Opening opn : openings) {
            deleteOpeningCascade(opn.getId());
        }
        teacherMapper.deleteById(teacherId);
    }

    @Override
    @Transactional
    public void deleteCourseCascade(Long courseId) {
        List<Opening> openings = openingMapper.selectList(
                new LambdaQueryWrapper<Opening>().eq(Opening::getCourseId, courseId));
        for (Opening opn : openings) {
            deleteOpeningCascade(opn.getId());
        }
        courseMapper.deleteById(courseId);
    }

    @Override
    @Transactional
    public void deleteOpeningCascade(Long openingId) {
        List<Selection> selections = selectionMapper.selectList(
                new LambdaQueryWrapper<Selection>().eq(Selection::getOpeningId, openingId));
        for (Selection sel : selections) {
            gradeMapper.delete(new LambdaQueryWrapper<Grade>().eq(Grade::getSelectionId, sel.getId()));
        }
        selectionMapper.delete(new LambdaQueryWrapper<Selection>().eq(Selection::getOpeningId, openingId));
        openingMapper.deleteById(openingId);
    }
}
