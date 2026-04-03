package com.student.system.dto;

public class SelectionResponse {
    private Long id;
    private Long openingId;
    private Long courseId;
    private String courseName;
    private Integer credit;
    private Long teacherId;
    private String teacherName;
    private String semester;

    public SelectionResponse() {}

    public SelectionResponse(Long id, Long openingId, Long courseId, String courseName, Integer credit,
                              Long teacherId, String teacherName, String semester) {
        this.id = id;
        this.openingId = openingId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.semester = semester;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOpeningId() { return openingId; }
    public void setOpeningId(Long openingId) { this.openingId = openingId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
