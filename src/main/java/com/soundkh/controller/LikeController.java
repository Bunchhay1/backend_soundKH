package com.soundkh.controller;

import com.soundkh.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracks/{trackId}")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long trackId,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(likeService.like(trackId, user.getUsername()));
    }

    @DeleteMapping("/like")
    public ResponseEntity<Map<String, Object>> unlike(@PathVariable Long trackId,
                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(likeService.unlike(trackId, user.getUsername()));
    }

    @GetMapping("/likes")
    public ResponseEntity<Map<String, Object>> count(@PathVariable Long trackId) {
        return ResponseEntity.ok(likeService.count(trackId));
    }
}
