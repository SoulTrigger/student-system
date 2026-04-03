package com.student.system.dto;

import java.time.LocalDateTime;

public class CourseResponse {
    private Long id;
    private String name;
    private Integer credit;
    private LocalDateTime createdAt;

    public CourseResponse() {}

    public CourseResponse(Long id, String name, Integer credit, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.credit = credit;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
