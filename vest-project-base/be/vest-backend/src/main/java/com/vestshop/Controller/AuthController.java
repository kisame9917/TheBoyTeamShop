package com.vestshop.Controller;

import com.vestshop.Service.AuthService;
import com.vestshop.dto.request.ForgotPasswordOtpRequest;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.request.ResetPasswordOtpRequest;
import com.vestshop.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.admin-frontend-url:http://localhost:5173}")
    private String adminFrontendUrl;

    @GetMapping("/google")
    public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession(true).setAttribute("oauth2_frontend_url", adminFrontendUrl);
        response.sendRedirect("/oauth2/authorization/google");
    }

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

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody ResetPasswordOtpRequest req) {
        authService.verifyOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.ok("OTP hợp lệ");
    }
}