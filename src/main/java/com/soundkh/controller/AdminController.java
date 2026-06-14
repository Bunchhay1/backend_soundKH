package com.soundkh.controller;

import com.soundkh.entity.Channel;
import com.soundkh.entity.User;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public AdminController(UserRepository userRepository, ChannelRepository channelRepository) {
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    /** GET /api/admin/users */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        var users = userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "role", u.getRole().name(),
                        "createdAt", u.getCreatedAt().toString()))
                .toList();
        return ResponseEntity.ok(users);
    }

    /** PATCH /api/admin/users/{id}/role?role=CREATOR */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> changeRole(@PathVariable Long id,
                                                           @RequestParam String role) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", id, "role", user.getRole().name()));
    }

    /** DELETE /api/admin/users/{id} — ban (delete) user */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/admin/channels/{id}/verify */
    @PatchMapping("/channels/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyChannel(@PathVariable Long id) {
        var channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        channel.setVerified(true);
        channelRepository.save(channel);
        return ResponseEntity.ok(Map.of("id", id, "isVerified", true));
    }
}
