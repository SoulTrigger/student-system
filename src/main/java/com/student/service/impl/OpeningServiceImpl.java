package com.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Opening;
import com.student.mapper.OpeningMapper;
import com.student.service.OpeningService;
import org.springframework.stereotype.Service;

@Service
public class OpeningServiceImpl extends ServiceImpl<OpeningMapper, Opening> implements OpeningService {}
