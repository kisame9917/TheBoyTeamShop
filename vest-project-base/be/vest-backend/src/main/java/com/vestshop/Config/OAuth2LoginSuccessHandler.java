package com.vestshop.Config;

import com.vestshop.Security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final JwtService jwtService;
    private final GoogleUserProvisionService googleUserProvisionService;

    @Qualifier("clientUserDetailsService")
    private final UserDetailsService clientUserDetailsService;

    @Qualifier("nhanVienUserDetailsService")
    private final UserDetailsService nhanVienUserDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name  = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=missing_email");
            return;
        }

        // ✅ ĐÚNG: findOrCreate trả về TargetUser (có target + username)
        GoogleUserProvisionService.TargetUser targetUser =
                googleUserProvisionService.findOrCreate(email, name);

        // ✅ load UserDetails theo targetUser.target()
        UserDetails userDetails = (targetUser.target() == GoogleUserProvisionService.LoginTarget.CLIENT)
                ? clientUserDetailsService.loadUserByUsername(targetUser.username())
                : nhanVienUserDetailsService.loadUserByUsername(targetUser.username());

        // generate JWT
        String token = jwtService.generateToken(userDetails);

        // redirect FE
        String redirectUrl = frontendUrl + "/oauth2/redirect?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}