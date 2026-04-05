-- Student System Database Schema
-- MySQL 8

CREATE DATABASE IF NOT EXISTS student_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE student_system;

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    credit INT NOT NULL CHECK (credit BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 开课表
CREATE TABLE IF NOT EXISTS opening (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 选课表
CREATE TABLE IF NOT EXISTS selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opening_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    FOREIGN KEY (opening_id) REFERENCES opening(id),
    FOREIGN KEY (student_id) REFERENCES student(id),
    UNIQUE KEY uk_opening_student (opening_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 成绩表
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    selection_id BIGINT NOT NULL,
    score INT CHECK (score IS NULL OR (score BETWEEN 0 AND 100)),
    semester VARCHAR(20),
    FOREIGN KEY (selection_id) REFERENCES selection(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation VARCHAR(100) NOT NULL,
    operator VARCHAR(50),
    operator_role VARCHAR(20),
    target_type VARCHAR(50),
    target_id BIGINT,
    detail VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
