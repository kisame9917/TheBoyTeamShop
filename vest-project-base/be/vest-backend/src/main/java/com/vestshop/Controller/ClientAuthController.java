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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.vestshop.Entity.DiaChiKhachHang;
import com.vestshop.Repository.DiaChiKhachHangRepository;
import com.vestshop.dto.response.DiaChiKhachHangResponse;
import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthService clientAuthService;
    private final KhachHangRepository khRepo;
    private final KhachHangService khachHangService;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    @Value("${app.client-frontend-url:http://localhost:5174}")
    private String clientFrontendUrl;

    @GetMapping("/google")
    public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession(true).setAttribute("oauth2_frontend_url", clientFrontendUrl);
        response.sendRedirect("/oauth2/authorization/google");
    }

    private KhachHang getCurrentCustomer(Authentication authentication) {
        String taiKhoan = authentication.getName();

        return khRepo.findByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }
//    private DiaChiKhachHangResponse mapDiaChi(DiaChiKhachHang d) {
//        if (d == null) return null;
//
//        return DiaChiKhachHangResponse.builder()
//                .id(d.getId())
//                .idKhachHang(d.getKhachHang() != null ? d.getKhachHang().getId() : null)
//                .tenNguoiNhan(d.getTenNguoiNhan())
//                .soDienThoai(d.getSoDienThoai())
//                .diaChiChiTiet(d.getDiaChiChiTiet())
//                .phuongXa(d.getPhuongXa())
//                .quanHuyen(d.getQuanHuyen())
//                .tinhThanh(d.getTinhThanh())
//                .quocGia(d.getQuocGia())
//                .laMacDinh(d.getLaMacDinh())
//                .trangThai(d.getTrangThai())
//                .build();
//    }
@GetMapping("/me/dia-chi")
public ResponseEntity<List<DiaChiKhachHangResponse>> myAddresses(Authentication authentication) {
    KhachHang kh = getCurrentCustomer(authentication);

    List<DiaChiKhachHangResponse> result = diaChiKhachHangRepository
            .findByKhachHangIdAndTrangThaiTrueOrderByLaMacDinhDescIdDesc(kh.getId())
            .stream()
            .map(this::mapDiaChi)
            .toList();

    return ResponseEntity.ok(result);
}

    private DiaChiKhachHangResponse mapDiaChi(DiaChiKhachHang d) {
        if (d == null) return null;

        return DiaChiKhachHangResponse.builder()
                .id(d.getId())
                .idKhachHang(d.getKhachHang() != null ? d.getKhachHang().getId() : null)
                .tenNguoiNhan(d.getTenNguoiNhan())
                .soDienThoai(d.getSoDienThoai())
                .diaChiChiTiet(d.getDiaChiChiTiet())
                .phuongXa(d.getPhuongXa())
                .quanHuyen(d.getQuanHuyen())
                .tinhThanh(d.getTinhThanh())
                .quocGia(d.getQuocGia())
                .laMacDinh(d.getLaMacDinh())
                .trangThai(d.getTrangThai())
                .build();
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