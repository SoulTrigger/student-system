package com.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Selection;
import com.student.mapper.SelectionMapper;
import com.student.service.SelectionService;
import org.springframework.stereotype.Service;

@Service
public class SelectionServiceImpl extends ServiceImpl<SelectionMapper, Selection> implements SelectionService {}
