package com.example.tradelens.service;


import com.example.tradelens.dto.RegisterRequest;
import com.example.tradelens.entity.User;
import com.example.tradelens.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {

        if(request.getUsername() == null
                || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if(request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if(request.getPassword() == null
                || request.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters"
            );
        }

        String username = request.getUsername().trim();

        String email = request.getEmail().trim()
                .toLowerCase(Locale.ROOT);

        if(userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole("USER");

        userRepository.save(user);

    }

}
