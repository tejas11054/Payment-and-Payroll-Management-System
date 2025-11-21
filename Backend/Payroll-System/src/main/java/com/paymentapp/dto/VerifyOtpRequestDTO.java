package com.paymentapp.dto;

import lombok.Data;

@Data
public class VerifyOtpRequestDTO {
    private String email;
    private String otp;
}