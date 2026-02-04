package com.vestshop.dto.request;

import lombok.Data;

@Data
public class ResetPasswordOtpRequest {
    private String email;
    private String otp;
    private String newPassword;
}
