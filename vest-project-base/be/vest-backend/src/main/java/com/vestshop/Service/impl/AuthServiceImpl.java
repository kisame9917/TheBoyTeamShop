package com.vestshop.Service.impl;

import com.vestshop.Security.JwtService;
import com.vestshop.Service.AuthService;
import com.vestshop.dto.request.LoginRequest;
import com.vestshop.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.taiKhoan(), req.matKhau())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        // dự án bạn 1 user = 1 role => lấy role đầu tiên
        String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new LoginResponse(token, role);
    }
}
