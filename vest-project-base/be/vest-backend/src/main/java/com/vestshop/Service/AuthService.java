package com.vestshop.Service;

import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest req);
    void sendOtp(String email);
    void resetPassword(String email, String otp, String newPassword);
    String genOtp6();
    String hashOtp(String emailLower, String otp);
    void verifyOtp(String email, String otp);

}
