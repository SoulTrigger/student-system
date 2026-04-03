package com.student.system.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileUpdateRequest {
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
