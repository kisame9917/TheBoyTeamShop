package com.vestshop.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Qualifier("clientUserDetailsService")
    private final UserDetailsService clientUserDetailsService;

    @Qualifier("nhanVienUserDetailsService")
    private final UserDetailsService nhanVienUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Chọn đúng userDetailsService theo route
                boolean isClientApi = request.getRequestURI().startsWith("/api/client/");
                UserDetailsService uds = isClientApi ? clientUserDetailsService : nhanVienUserDetailsService;

                UserDetails user = uds.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Bỏ qua hoàn toàn OAuth2 endpoints
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) return true;

        // Bỏ qua Swagger (optional)
        if (path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) return true;

        // Bỏ qua auth endpoints của bạn (optional)
        if (path.startsWith("/api/auth/")) return true;

        // Static
        if (path.startsWith("/uploads/") || path.startsWith("/images/")) return true;

        return false;
    }
}