package com.uber_clone.auth;

import java.util.Optional;
import com.uber_clone.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.uber_clone.repository.UserRepository;

// Jako że korzystam z sql

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    // Wymagane ponieważ obsługujemy to ze swoim modelem User
    // A nie z modelem spring security
    @Override
    public UserDetails loadUserByUsername(String userEmail) {

        Optional<User> user = repository.findByEmail(userEmail);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User doesn't exist");
        }

        // user.get() bo user jest Optional<User>
        User dbUser = user.get();

        return org.springframework.security.core.userdetails.User
                .withUsername(dbUser.getEmail())
                .password(dbUser.getPassword())
                // role to enum więc .name() zamienia to w tekst
                .roles(dbUser.getRole().name())
                // build zatwierdza i buduje "formularz"
                .build();

    }

}
