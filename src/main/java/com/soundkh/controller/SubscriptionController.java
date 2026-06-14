package com.soundkh.controller;

import com.soundkh.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /** POST /api/subscriptions/activate — activate SuperStar (call after payment) */
    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(subscriptionService.activate(user.getUsername()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(subscriptionService.status(user.getUsername()));
    }
}
