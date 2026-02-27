package com.vestshop.Config;

import com.vestshop.Security.ClientUserDetailsService;
import com.vestshop.Service.impl.NhanVienUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AuthManagersConfig {

    // ✅ 1 encoder duy nhất (plain text)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean("adminAuthProvider")
    public DaoAuthenticationProvider adminAuthProvider(
            NhanVienUserDetailsService nhanVienUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(nhanVienUserDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    @Bean("clientAuthProvider")
    public DaoAuthenticationProvider clientAuthProvider(
            ClientUserDetailsService clientUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(clientUserDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    /**
     * ✅ AuthenticationManager GLOBAL cho HttpSecurity (bắt buộc có 1 cái Primary)
     * Nó sẽ thử authenticate qua admin provider rồi đến client provider.
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(
            @Qualifier("adminAuthProvider") DaoAuthenticationProvider adminProvider,
            @Qualifier("clientAuthProvider") DaoAuthenticationProvider clientProvider
    ) {
        return new ProviderManager(List.of(adminProvider, clientProvider));
    }

    @Bean(name = "adminAuthenticationManager")
    public AuthenticationManager adminAuthenticationManager(
            @Qualifier("adminAuthProvider") DaoAuthenticationProvider p
    ) {
        return new ProviderManager(p);
    }

    @Bean(name = "clientAuthenticationManager")
    public AuthenticationManager clientAuthenticationManager(
            @Qualifier("clientAuthProvider") DaoAuthenticationProvider p
    ) {
        return new ProviderManager(p);
    }
}