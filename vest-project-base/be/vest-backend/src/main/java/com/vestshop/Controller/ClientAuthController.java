package com.vestshop.Controller;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Service.ClientAuthService;
import com.vestshop.Service.KhachHangService;
import com.vestshop.dto.request.ForgotPasswordOtpRequest;
import com.vestshop.dto.request.KhachHangRequest;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.request.ResetPasswordOtpRequest;
import com.vestshop.dto.response.ClientLoginResponse;
import com.vestshop.dto.response.KhachHangResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthService clientAuthService;
    private final KhachHangRepository khRepo;
    private final KhachHangService khachHangService;

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    private KhachHang getCurrentCustomer(Authentication authentication) {
        String taiKhoan = authentication.getName();

        return khRepo.findByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    @GetMapping("/me")
    public ResponseEntity<KhachHangResponse> me(Authentication authentication) {
        KhachHang kh = getCurrentCustomer(authentication);
        return ResponseEntity.ok(khachHangService.getById(kh.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<KhachHangResponse> updateMe(
            Authentication authentication,
            @RequestBody KhachHangRequest request
    ) {
        KhachHang kh = getCurrentCustomer(authentication);

        request.setTaiKhoan(null);
        request.setTrangThai(null);
        request.setMatKhau(null);

        return ResponseEntity.ok(khachHangService.update(kh.getId(), request));
    }

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