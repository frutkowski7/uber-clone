package com.uber_clone.auth;

import java.util.Optional;

import com.uber_clone.Request.LoginRequest;
import com.uber_clone.Request.RegisterRequest;
import com.uber_clone.Response.AuthenticationResponse;
import com.uber_clone.Response.LoginResponse;
import com.uber_clone.Response.RegisterResponse;
import com.uber_clone.exceptions.EmailAlreadyExistsException;
import com.uber_clone.exceptions.EmailNotFoundException;
import com.uber_clone.model.User;
import com.uber_clone.model.User.Role;
import com.uber_clone.repository.UserRepository;
import com.uber_clone.service.DriverService;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DriverService driverService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService detailsService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // email
        Optional<User> userData = userRepository.findByEmail(request.getEmail());
        if (!userData.isEmpty()) {
            throw new EmailAlreadyExistsException("User already exists!");
        }

        var savedUser = userRepository
                .save(new User(null, request.getEmail(), passwordEncoder.encode(request.getPassword()),
                        User.Role.valueOf(request.getRole().name())));

        if (request.getRole() == Role.DRIVER) {
            driverService.addDriver(request.getEmail());
        }

        var jwtToken = jwtService.generateToken(savedUser);

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), jwtToken);

    }

    /*
     * 
     * @RequiredArgsConstructor @Setter
     * Kiedy działa? Przy tworzeniu obiektu Po utworzeniu obiektu
     * Do czego służy? Przekazuje wymagane wartości Zmienia wartości pól
     * Wymaga final? Nie zawsze, ale obejmuje pola final Nie
     * 
     */

    public AuthenticationResponse authenticate(RegisterRequest request) {

        // Czy podany email i hasło należą do użytkownika?
        // Auth manager przekazuje ten obiekt do providera
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()

                ));
        UserDetails userDetails = detailsService.loadUserByUsername(request.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(jwtToken);

    }

    public LoginResponse login(LoginRequest request) {

        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            throw new EmailNotFoundException("User doesn't exist!");
        }

        if (verifyPassword(request.getPassword(), user.get().getPassword())) {
            return new LoginResponse(
                    user.get().getId(),
                    user.get().getEmail());
        }

        // null mogę przypisać typom referencyjnym
        // String może być obiektem tekstowym albo null

        return new LoginResponse(null, null);

    }

    public boolean verifyPassword(String rawPassword, String storedPasswordHash) {
        return passwordEncoder.matches(rawPassword, storedPasswordHash);
    }

}
