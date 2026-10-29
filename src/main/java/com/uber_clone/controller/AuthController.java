package com.uber_clone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uber_clone.Request.LoginRequest;
import com.uber_clone.Request.RegisterRequest;
import com.uber_clone.Response.AuthenticationResponse;
import com.uber_clone.Response.LoginResponse;
import com.uber_clone.Response.RegisterResponse;
import com.uber_clone.auth.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        // nie muszę rzucać wyjątku jeżeli jest już tutaj
        return ResponseEntity.ok(authService.register(request));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));

    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

}
