package com.vestshop.Config;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleUserProvisionService {

    public enum LoginTarget { CLIENT, NHANVIEN }

    public record TargetUser(LoginTarget target, String username) {}

    private final KhachHangRepository khRepo;

    @Transactional
    public TargetUser findOrCreate(String email, String name) {
        String emailNorm = normalizeEmail(email);

        if (emailNorm == null || emailNorm.isBlank()) {
            throw new IllegalArgumentException("Email Google không hợp lệ");
        }

        KhachHang khByEmail = khRepo.findByEmailIgnoreCase(emailNorm).orElse(null);
        if (khByEmail != null) {
            return new TargetUser(LoginTarget.CLIENT, khByEmail.getTaiKhoan());
        }

        KhachHang khByTaiKhoan = khRepo.findByTaiKhoanIgnoreCase(emailNorm).orElse(null);
        if (khByTaiKhoan != null) {
            return new TargetUser(LoginTarget.CLIENT, khByTaiKhoan.getTaiKhoan());
        }

        KhachHang created = createNewKhachHang(emailNorm, name);
        return new TargetUser(LoginTarget.CLIENT, created.getTaiKhoan());
    }

    private KhachHang createNewKhachHang(String emailNorm, String name) {
        KhachHang newKh = new KhachHang();

        String ten = (name != null && !name.isBlank())
                ? name.trim()
                : emailNorm.substring(0, emailNorm.indexOf("@"));

        LocalDateTime now = LocalDateTime.now();

        newKh.setTaiKhoan(emailNorm);
        newKh.setEmail(emailNorm);
        newKh.setTenKhachHang(ten);
        newKh.setGioiTinh(false);
        newKh.setMatKhau(UUID.randomUUID().toString());
        newKh.setNgayTao(now);
        newKh.setNgayCapNhat(now);
        newKh.setTrangThai(true);
        newKh.setMaKhachHang(nextMaKhachHang());

        return khRepo.save(newKh);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private synchronized String nextMaKhachHang() {
        String prefix = "KH";

        KhachHang last = khRepo
                .findTopByMaKhachHangStartingWithOrderByMaKhachHangDesc(prefix)
                .orElse(null);

        int next = 1;

        if (last != null && last.getMaKhachHang() != null && !last.getMaKhachHang().isBlank()) {
            String digits = last.getMaKhachHang().replaceAll("[^0-9]", "");
            if (!digits.isBlank()) {
                try {
                    next = Integer.parseInt(digits) + 1;
                } catch (Exception ignored) {
                }
            }
        }

        String code;
        do {
            code = prefix + String.format("%03d", next);
            next++;
        } while (khRepo.existsByMaKhachHang(code));

        return code;
    }
}