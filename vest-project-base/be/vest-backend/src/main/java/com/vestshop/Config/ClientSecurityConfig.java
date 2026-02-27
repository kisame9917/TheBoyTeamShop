package com.vestshop.Config;

import com.vestshop.Security.ClientUserDetailsService;
import com.vestshop.Security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class ClientSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public DaoAuthenticationProvider clientAuthProvider(
            ClientUserDetailsService clientUserDetailsService,
            PasswordEncoder passwordEncoder  // lấy bean chung từ SecurityConfig
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(clientUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean(name = "clientAuthenticationManager")
    public AuthenticationManager clientAuthenticationManager(
            DaoAuthenticationProvider clientAuthProvider
    ) {
        return new ProviderManager(clientAuthProvider);
    }

    @Bean
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/client/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC: catalog
                        .requestMatchers(HttpMethod.GET,
                                "/api/client/home",
                                "/api/client/products/**"
                        ).permitAll()

                        // PUBLIC: auth client
                        .requestMatchers("/api/client/auth/**").permitAll()

                        // PRIVATE: còn lại phải login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}