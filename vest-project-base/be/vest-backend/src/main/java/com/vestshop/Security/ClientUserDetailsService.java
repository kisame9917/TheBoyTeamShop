package com.vestshop.Security;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientUserDetailsService implements UserDetailsService {

    private final KhachHangRepository khRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        KhachHang kh = khRepo.findByTaiKhoan(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng"));

        return new org.springframework.security.core.userdetails.User(
                kh.getTaiKhoan(),
                kh.getMatKhau(), // plain text nếu bạn không hash
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
    }
}