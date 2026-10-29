package com.uber_clone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.uber_clone.auth.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                // csrf - cross site request forgery

                // SecurityFilterChain ustawia reguły bezpieczeństwa Springa
                // dla danych żądań HTTP

                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(
                                                jwtAuthFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}

/*
 * Normal Spring Security Flow (Session-Based Authentication)
 * 
 * 
 * Security Filter Chain intercepts the login request
 * Creates an Unauthenticated Authentication object (with only
 * username/password)
 * Passes it to the AuthenticationManager
 * AuthenticationManager delegates to DaoAuthenticationProvider
 * DaoAuthenticationProvider loads user from DB and verifies password
 * Returns an Authenticated Authentication object
 * Spring Security stores this Authentication in SecurityContext
 * Spring Security then creates an HTTP Session
 * On every next request, Spring retrieves Authentication from the session →
 * user stays logged in
 * 
 */