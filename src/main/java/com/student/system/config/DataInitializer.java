package com.student.system.config;

import com.student.system.entity.Teacher;
import com.student.system.mapper.TeacherMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long count = teacherMapper.selectCount(new QueryWrapper<Teacher>().eq("name", "admin"));
        if (count == 0) {
            Teacher admin = new Teacher();
            admin.setName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            teacherMapper.insert(admin);
        }
    }
}
