package com.olimpo.dto;

import lombok.Data;

@Data
public class PasswordResetDto {
    private String token;
    private String newPassword;
    private String confirmPassword;
}