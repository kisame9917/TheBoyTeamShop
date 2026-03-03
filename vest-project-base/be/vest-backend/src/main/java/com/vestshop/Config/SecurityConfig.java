package com.vestshop.Config;

import com.vestshop.Security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(2)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // <-- THÊM

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

//                        // --- THÊM 2 dòng này để OAuth2 chạy ---
//                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/uploads/**", "/images/**").permitAll()

                        .requestMatchers("/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/khach-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/ca-lam-viec/lich-ca-nhan/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/pgg/**").hasAnyRole("ADMIN","STAFF")
                        .requestMatchers("/api/giao-ca/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/giao-ca/**").hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().hasRole("ADMIN")
                )
                // --- THÊM oauth2Login để login Google ---
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}