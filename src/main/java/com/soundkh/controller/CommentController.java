package com.soundkh.controller;

import com.soundkh.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/tracks/{trackId}/comments")
    public ResponseEntity<Map<String, Object>> post(@PathVariable Long trackId,
                                                     @RequestBody Map<String, String> body,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(commentService.post(trackId, body.get("content"), user.getUsername()));
    }

    @GetMapping("/api/tracks/{trackId}/comments")
    public ResponseEntity<Page<Map<String, Object>>> list(@PathVariable Long trackId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(commentService.list(trackId, page, size));
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<Map<String, Object>> edit(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(commentService.edit(id, body.get("content"), user.getUsername()));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        commentService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
