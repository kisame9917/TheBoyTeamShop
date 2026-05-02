package com.vestshop.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanVienChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private String otp;
}