package com.paymentapp.dto;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String email;
    private String otp;
    private String newPassword;
}