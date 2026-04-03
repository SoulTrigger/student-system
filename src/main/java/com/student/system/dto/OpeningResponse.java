package com.student.system.dto;

public class OpeningResponse {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String semester;

    public OpeningResponse(Long id, Long courseId, String courseName, Long teacherId, String teacherName, String semester) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.semester = semester;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public Long getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getSemester() { return semester; }
}
