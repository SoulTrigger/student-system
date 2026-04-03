package com.student.system.dto;

public class GradeResponse {
    private Long id;
    private Long selectionId;
    private Integer score;
    private String semester;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;

    public GradeResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSelectionId() { return selectionId; }
    public void setSelectionId(Long selectionId) { this.selectionId = selectionId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
