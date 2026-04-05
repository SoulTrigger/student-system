package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("selection")
public class Selection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long openingId;
    private Long studentId;
}
