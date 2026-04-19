package com.vestshop.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@Order(0)
public class OAuth2SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String defaultFrontendUrl;

    @Bean
    public SecurityFilterChain oauth2Chain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();

                            String targetFrontendUrl = (String) request.getSession()
                                    .getAttribute("oauth2_frontend_url");

                            if (targetFrontendUrl == null || targetFrontendUrl.isBlank()) {
                                targetFrontendUrl = defaultFrontendUrl;
                            }

                            request.getSession().removeAttribute("oauth2_frontend_url");
                            response.sendRedirect(targetFrontendUrl + "/login?error=oauth2_failed");
                        })
                );

        return http.build();
    }
}