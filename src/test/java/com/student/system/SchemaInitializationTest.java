package com.student.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SchemaInitializationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testStudentTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'STUDENT'", Integer.class);
        assertTrue(count != null && count > 0, "STUDENT table should exist");
    }

    @Test
    void testTeacherTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TEACHER'", Integer.class);
        assertTrue(count != null && count > 0, "TEACHER table should exist");
    }

    @Test
    void testCourseTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'COURSE'", Integer.class);
        assertTrue(count != null && count > 0, "COURSE table should exist");
    }

    @Test
    void testOpeningTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'OPENING'", Integer.class);
        assertTrue(count != null && count > 0, "OPENING table should exist");
    }

    @Test
    void testSelectionTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SELECTION'", Integer.class);
        assertTrue(count != null && count > 0, "SELECTION table should exist");
    }

    @Test
    void testGradeTableExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'GRADE'", Integer.class);
        assertTrue(count != null && count > 0, "GRADE table should exist");
    }

    @Test
    void testAdminUserInserted() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM teacher WHERE name = 'admin'", Integer.class);
        assertTrue(count != null && count > 0, "Default admin user should be inserted");
    }

    @Test
    void testAdminPasswordIsBcrypted() {
        String password = jdbcTemplate.queryForObject(
            "SELECT password FROM teacher WHERE name = 'admin'", String.class);
        assertTrue(password != null && password.startsWith("$2a$"), 
            "Admin password should be BCrypt hashed");
    }
}
