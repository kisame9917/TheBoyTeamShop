package com.vestshop.Config;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        // ✅ 1) Nếu email đã tồn tại => lấy đúng record DB và RETURN NGAY, KHÔNG UPDATE, KHÔNG SAVE
        KhachHang khByEmail = khRepo.findByEmailIgnoreCase(emailNorm).orElse(null);
        if (khByEmail != null) {
            // dùng taiKhoan hiện tại trong DB để login (không ép taiKhoan=email)
            return new TargetUser(LoginTarget.CLIENT, khByEmail.getTaiKhoan());
        }

        // ✅ 2) Nếu chưa có theo email, thử theo taiKhoan=email (case trước đó bạn tạo)
        KhachHang khByTaiKhoan = khRepo.findByTaiKhoan(emailNorm).orElse(null);
        if (khByTaiKhoan != null) {
            return new TargetUser(LoginTarget.CLIENT, khByTaiKhoan.getTaiKhoan());
        }

        // ✅ 3) Chưa có => tạo mới. Có chống race condition (unique email)
        try {
            KhachHang created = createNewKhachHang(emailNorm, name);
            return new TargetUser(LoginTarget.CLIENT, created.getTaiKhoan());
        } catch (DataIntegrityViolationException ex) {
            // nếu request khác vừa insert trước -> lấy lại theo email và return
            KhachHang existed = khRepo.findByEmailIgnoreCase(emailNorm).orElse(null);
            if (existed != null) {
                return new TargetUser(LoginTarget.CLIENT, existed.getTaiKhoan());
            }
            throw ex;
        }
    }

    private KhachHang createNewKhachHang(String emailNorm, String name) {
        KhachHang newKh = new KhachHang();

        String ten = (name != null && !name.isBlank())
                ? name.trim()
                : emailNorm.substring(0, emailNorm.indexOf("@"));

        LocalDateTime now = LocalDateTime.now();

        // Quy ước cho user mới: taiKhoan = email
        newKh.setTaiKhoan(emailNorm);
        newKh.setEmail(emailNorm);
        newKh.setTenKhachHang(ten);

        // NOT NULL theo DB của bạn
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

    private String nextMaKhachHang() {
        String max = khRepo.findMaxMaKhachHang();
        if (max == null || max.isBlank()) return "KH000001";

        String digits = max.replaceAll("\\D+", "");
        int num = 0;
        try { num = Integer.parseInt(digits); } catch (Exception ignored) {}

        num += 1;
        return String.format("KH%06d", num);
    }
}