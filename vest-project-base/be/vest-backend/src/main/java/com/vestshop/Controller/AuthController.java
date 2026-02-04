package com.vestshop.Controller;

import com.vestshop.Service.AuthService;
import com.vestshop.dto.request.ForgotPasswordOtpRequest;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.request.ResetPasswordOtpRequest;
import com.vestshop.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> forgot(@RequestBody ForgotPasswordOtpRequest req) {
        try {
            authService.sendOtp(req.getEmail());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception ignore) {
            // luôn trả OK để tránh lộ email tồn tại
        }
        return ResponseEntity.ok("OK");
    }
    @PostMapping("/reset-password-otp")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordOtpRequest req) {
        authService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}