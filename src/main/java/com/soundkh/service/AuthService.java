package com.soundkh.service;

import com.soundkh.dto.AuthDto;
import com.soundkh.entity.User;
import com.soundkh.repository.UserRepository;
import com.soundkh.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authManager = authManager;
    }

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already registered");

        var user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(user);

        String token = jwtUtil.generate(user.getUsername(), user.getRole().name());
        return new AuthDto.AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var user = userRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtUtil.generate(user.getUsername(), user.getRole().name());
        return new AuthDto.AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
