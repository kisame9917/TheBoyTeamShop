package com.vestshop.Service.impl;

import com.vestshop.Entity.KhachHang;
import com.vestshop.Repository.KhachHangRepository;
import com.vestshop.Security.JwtService;
import com.vestshop.Service.ClientAuthService;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.ClientLoginResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthServiceImpl implements ClientAuthService {

    private final KhachHangRepository khrepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ClientAuthServiceImpl(
            KhachHangRepository khrepo,
            JwtService jwtService,
            @Qualifier("clientAuthenticationManager") AuthenticationManager authenticationManager
    ) {
        this.khrepo = khrepo;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public ClientLoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.taiKhoan(), req.matKhau())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        KhachHang kh = khrepo.findByTaiKhoan(req.taiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        return new ClientLoginResponse(
                token,
                kh.getId(),
                kh.getTaiKhoan(),
                kh.getAnhDaiDien(),
                kh.getTenKhachHang()
        );
    }
}