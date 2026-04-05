package com.student.service;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String idStr, String password, String role);
    void changePassword(Long userId, String role, String oldPassword, String newPassword);
}
