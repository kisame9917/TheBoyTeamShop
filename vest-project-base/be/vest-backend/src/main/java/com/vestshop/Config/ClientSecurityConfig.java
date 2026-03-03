package com.vestshop.Config;

import com.vestshop.Security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Order(1)
public class ClientSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/client/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public pages/api
                        .requestMatchers(HttpMethod.GET, "/api/client/home", "/api/client/products/**").permitAll()

                        // ✅ permit đúng các endpoint auth cần public
                        .requestMatchers(
                                "/api/client/auth/login",
                                "/api/client/auth/google",
                                "/api/client/auth/forgot-password-otp",
                                "/api/client/auth/reset-password-otp",
                                "/api/client/auth/verify-otp"
                        ).permitAll()

                        // ✅ /me bắt buộc có token
                        .requestMatchers(HttpMethod.GET, "/api/client/auth/me").authenticated()

                        // còn lại trong /api/client/** phải đăng nhập
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}