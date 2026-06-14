package com.soundkh.controller;

import com.soundkh.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificationService.list(user.getUsername()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificationService.markRead(id, user.getUsername()));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAll(@AuthenticationPrincipal UserDetails user) {
        notificationService.clearAll(user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
