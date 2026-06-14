package com.soundkh.controller;

import com.soundkh.service.ChannelFollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/channels/{id}/follow")
public class ChannelFollowController {

    private final ChannelFollowService followService;

    public ChannelFollowController(ChannelFollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    public ResponseEntity<Void> follow(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        followService.follow(id, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails user) {
        followService.unfollow(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("followers", followService.followerCount(id)));
    }
}
