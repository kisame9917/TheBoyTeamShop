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
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/gemini/**").permitAll()
                        // auth + oauth2 + swagger
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // static files
                        .requestMatchers("/uploads/**", "/images/**").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()

                        // online checkout public
                        .requestMatchers("/api/checkout/**", "/api/online-checkout/**").permitAll()

                        // shop online public APIs
                        .requestMatchers(HttpMethod.GET,
                                "/api/san-pham",
                                "/api/san-pham/**",
                                "/api/loai-san-pham",
                                "/api/loai-san-pham/**",
                                "/api/san-pham-chi-tiet/by-product/**",
                                "/api/pgg/pos"
                        ).permitAll()

                        // backoffice / bán hàng tại quầy
                        .requestMatchers("/api/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/khach-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/ca-lam-viec/lich-ca-nhan/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/pgg/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/giao-ca/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/giao-ca/**").hasAnyRole("ADMIN", "STAFF")

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}