package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String password;
    private LocalDateTime createdAt;
}
