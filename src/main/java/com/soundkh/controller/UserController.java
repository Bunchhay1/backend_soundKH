package com.soundkh.controller;

import com.soundkh.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** GET /api/users/me */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(toMap(user));
    }

    /** GET /api/users/{username} — public profile */
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> profile(@PathVariable String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "bio", user.getBio() != null ? user.getBio() : "",
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                "role", user.getRole().name()
        ));
    }

    /** PUT /api/users/me — update bio / avatarUrl */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> update(
            @Valid @RequestBody UpdateRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (req.bio() != null) user.setBio(req.bio());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        userRepository.save(user);
        return ResponseEntity.ok(toMap(user));
    }

    /** PUT /api/users/me/password */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect");
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(com.soundkh.entity.User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "bio", user.getBio() != null ? user.getBio() : "",
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt().toString()
        );
    }

    public record UpdateRequest(String bio, String avatarUrl) {}

    public record PasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6) String newPassword
    ) {}
}
