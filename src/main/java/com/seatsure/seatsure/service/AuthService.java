package com.seatsure.seatsure.service;

import com.seatsure.seatsure.dto.AuthResponse;
import com.seatsure.seatsure.dto.LoginRequest;
import com.seatsure.seatsure.dto.RegisterRequest;
import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.UserRepository;
import com.seatsure.seatsure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        // NEVER store the raw password - only its BCrypt hash.
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(User.Role.USER);

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(token, saved.getEmail(), saved.getFullName(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // matches() re-hashes the raw input with the SAME salt embedded in
        // the stored hash, and compares - this is how BCrypt verification
        // works without ever needing to "decrypt" the stored hash (it can't;
        // hashing is one-way by design).
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}