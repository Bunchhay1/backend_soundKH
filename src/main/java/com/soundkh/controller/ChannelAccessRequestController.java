package com.soundkh.controller;

import com.soundkh.service.ChannelAccessRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channel-access-requests")
public class ChannelAccessRequestController {

    private final ChannelAccessRequestService service;

    public ChannelAccessRequestController(ChannelAccessRequestService service) {
        this.service = service;
    }

    /** Listener requests access to a private channel */
    @PostMapping("/channels/{channelId}")
    public ResponseEntity<Map<String, Object>> request(@PathVariable Long channelId,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.request(channelId, user.getUsername()));
    }

    /** Channel owner lists pending requests for their channels */
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> listPending(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.listPending(user.getUsername()));
    }

    /** Channel owner approves or rejects a request */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                             @RequestParam String status,
                                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.updateStatus(id, status, user.getUsername()));
    }
}
