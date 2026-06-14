package com.soundkh.controller;

import com.soundkh.service.AccessRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    public AccessRequestController(AccessRequestService accessRequestService) {
        this.accessRequestService = accessRequestService;
    }

    @PostMapping("/tracks/{trackId}")
    public ResponseEntity<Map<String, Object>> request(@PathVariable Long trackId,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(accessRequestService.request(trackId, user.getUsername()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> listPending(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(accessRequestService.listPending(user.getUsername()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                             @RequestParam String status,
                                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(accessRequestService.updateStatus(id, status, user.getUsername()));
    }
}
