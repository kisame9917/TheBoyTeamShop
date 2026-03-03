package com.vestshop.Controller;

import com.vestshop.Service.ClientAuthService;
import com.vestshop.dto.request.ForgotPasswordOtpRequest;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.request.ResetPasswordOtpRequest;
import com.vestshop.dto.response.ClientLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthService clientAuthService;

    @PostMapping("/login")
    public ResponseEntity<ClientLoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(clientAuthService.login(req));
    }
    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> forgot(@RequestBody ForgotPasswordOtpRequest req) {
        try {
            clientAuthService.sendOtp(req.getEmail());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception ignore) {
            // luôn trả OK để tránh lộ email tồn tại
        }
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/reset-password-otp")
    public ResponseEntity<?> reset(@RequestBody ResetPasswordOtpRequest req) {
        clientAuthService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody ResetPasswordOtpRequest req) {
        clientAuthService.verifyOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.ok("OTP hợp lệ");
    }
}