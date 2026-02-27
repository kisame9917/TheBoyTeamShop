package com.vestshop.Service.impl;

import com.vestshop.Entity.NhanVien;
import com.vestshop.Entity.PasswordResetOtp;
import com.vestshop.Repository.NhanVienRepository;
import com.vestshop.Repository.PasswordResetOtpRepository;
import com.vestshop.Security.JwtService;
import com.vestshop.Service.AuthService;
import com.vestshop.Service.EmailService;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.LoginResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordResetOtpRepository otpRepo;
    private final NhanVienRepository nhanVienRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.otp.secret:CHANGE_ME_SECRET}")
    private String otpSecret;

    private static final int OTP_EXPIRE_MIN = 10;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SEC = 60;

    public AuthServiceImpl(
            PasswordResetOtpRepository otpRepo,
            NhanVienRepository nhanVienRepository,
            EmailService emailService,
            @Qualifier("adminAuthenticationManager") AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.otpRepo = otpRepo;
        this.nhanVienRepository = nhanVienRepository;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.taiKhoan(), req.matKhau())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        NhanVien nv = nhanVienRepository.findByTaiKhoan(req.taiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

        return new LoginResponse(
                token,
                role,
                nv.getId(),
                nv.getTenNhanVien(),
                nv.getEmail()
        );
    }

    @Override
    public void sendOtp(String email) {
        if (email == null || email.isBlank()) return;
        String em = email.trim();

        NhanVien nv = nhanVienRepository.findByEmail(em).orElse(null);
        if (nv == null) return;

        if (Boolean.FALSE.equals(nv.getTrangThai())) return;

        var lastOpt = otpRepo.findTopByEmailOrderByIdDesc(em);
        if (lastOpt.isPresent() && lastOpt.get().getLastSentAt() != null) {
            long sec = Duration.between(lastOpt.get().getLastSentAt(), LocalDateTime.now()).getSeconds();
            if (sec < RESEND_COOLDOWN_SEC) {
                throw new IllegalStateException("Vui lòng chờ " + (RESEND_COOLDOWN_SEC - sec) + " giây để gửi lại OTP");
            }
        }

        String otp = genOtp6();
        String otpHash = hashOtp(em.toLowerCase(), otp);

        PasswordResetOtp row = new PasswordResetOtp();
        row.setNhanVienId(nv.getId());
        row.setEmail(em);
        row.setOtpHash(otpHash);
        row.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MIN));
        row.setUsed(false);
        row.setAttempts(0);
        row.setLastSentAt(LocalDateTime.now());
        otpRepo.save(row);

        emailService.sendResetPasswordOtp(em, nv.getTenNhanVien(), otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        getValidOtpRowOrThrow(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        if (email == null || email.isBlank()) throw new RuntimeException("Email không hợp lệ");
        if (otp == null || otp.isBlank()) throw new RuntimeException("OTP không hợp lệ");
        if (newPassword == null || newPassword.length() < 6) throw new RuntimeException("Mật khẩu tối thiểu 6 ký tự");

        String em = email.trim();

        PasswordResetOtp row = otpRepo.findTopByEmailOrderByIdDesc(em)
                .orElseThrow(() -> new RuntimeException("OTP không tồn tại hoặc đã hết hạn"));

        if (Boolean.TRUE.equals(row.getUsed())) throw new RuntimeException("OTP đã được sử dụng");
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("OTP đã hết hạn");
        if (row.getAttempts() != null && row.getAttempts() >= OTP_MAX_ATTEMPTS) {
            throw new RuntimeException("Bạn đã nhập sai quá nhiều lần");
        }

        String inputHash = hashOtp(em.toLowerCase(), otp.trim());
        if (!inputHash.equals(row.getOtpHash())) {
            row.setAttempts((row.getAttempts() == null ? 0 : row.getAttempts()) + 1);
            otpRepo.save(row);
            throw new RuntimeException("OTP không đúng");
        }

        NhanVien nv = nhanVienRepository.findByEmail(em)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        nv.setMatKhau(newPassword);
        nhanVienRepository.save(nv);

        row.setUsed(true);
        otpRepo.save(row);
    }

    private void getValidOtpRowOrThrow(String email, String otp) {
        if (email == null || email.isBlank()) throw new RuntimeException("Email không hợp lệ");
        if (otp == null || otp.isBlank()) throw new RuntimeException("OTP không hợp lệ");

        String em = email.trim();
        PasswordResetOtp row = otpRepo.findTopByEmailOrderByIdDesc(em)
                .orElseThrow(() -> new RuntimeException("OTP không tồn tại hoặc đã hết hạn"));

        if (Boolean.TRUE.equals(row.getUsed())) throw new RuntimeException("OTP đã được sử dụng");
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("OTP đã hết hạn");

        String inputHash = hashOtp(em.toLowerCase(), otp.trim());
        if (!inputHash.equals(row.getOtpHash())) {
            throw new RuntimeException("OTP không đúng");
        }
    }

    @Override
    public String genOtp6() {
        int n = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", n);
    }

    @Override
    public String hashOtp(String emailLower, String otp) {
        try {
            String raw = otpSecret + "|" + emailLower + "|" + otp;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}