package com.student.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("selection")
public class Selection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long openingId;
    private Long studentId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOpeningId() { return openingId; }
    public void setOpeningId(Long openingId) { this.openingId = openingId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
