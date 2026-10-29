
package com.uber_clone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uber_clone.auth.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    // Nasza usługa wyszukująca użytkownika w bazie danych.
    private final CustomUserDetailsService userDetailsService;

    @Bean
    // Rejestruje PasswordEncoder jako bean dostępny do wstrzykiwania w innych
    // klasach.
    public PasswordEncoder passwordEncoder() {

        // BCrypt bezpiecznie hashuje hasła i pozwala później je weryfikować.
        return new BCryptPasswordEncoder();

    }

    /*
     * Co więc sprawdza hasło? Standardowo, jeśli skonfigurowaliśmy
     * DaoAuthenticationProvider, to właśnie on:
     * 1. Wywołuje CustomUserDetailsService.loadUserByUsername(email), aby pobrać
     * użytkownika i jego hash.
     * 2. Korzysta z skonfigurowanego PasswordEncoder, wywołując
     * matches(rawPassword, storedHash).
     * 3. Jeśli hasło się zgadza, uwierzytelnianie kończy się sukcesem. W przeciwnym
     * razie rzucany jest wyjątek.
     * 
     */

    @Bean
    // Provider odpowiada za sprawdzenie danych logowania użytkownika.
    public AuthenticationProvider authenticationProvider() {

        // Provider korzysta z naszej usługi, aby znaleźć użytkownika,
        // np. po adresie e-mail.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // Ustawiamy sposób weryfikacji hasła.
        // Spring porówna podane hasło z hashem zapisanym w bazie.
        authProvider.setPasswordEncoder(passwordEncoder());

        // Zwracamy gotowy provider, aby Spring mógł go wykorzystać.
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
