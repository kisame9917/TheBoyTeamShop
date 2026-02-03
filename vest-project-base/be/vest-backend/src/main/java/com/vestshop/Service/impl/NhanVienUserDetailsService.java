package com.vestshop.Service.impl;

import com.vestshop.Entity.NhanVien;
import com.vestshop.Repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NhanVienUserDetailsService implements UserDetailsService {

    private final NhanVienRepository nhanVienRepository;

    @Override
    public UserDetails loadUserByUsername(String taiKhoan) throws UsernameNotFoundException {
        NhanVien nv = nhanVienRepository.findByTaiKhoan(taiKhoan)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy: " + taiKhoan));

        String dbRole = nv.getQuyenHan().getMaQuyenHan(); // VD: QH_ADMIN / QH_NV

        String role = switch (dbRole) {
            case "QH_ADMIN", "ADMIN" -> "ADMIN";
            case "QH_NV", "STAFF" -> "STAFF";
            default -> dbRole; // fallback
        };

        // roles("ADMIN") => tự thêm prefix ROLE_
        return User.builder()
                .username(nv.getTaiKhoan())
                .password(nv.getMatKhau())
                .roles(role)  // ROLE_ADMIN / ROLE_STAFF
                .build();
    }
}
