package com.soundkh.controller;

import com.soundkh.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestParam String name,
                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(playlistService.create(name, user.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listMineRoot(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(playlistService.listMine(user.getUsername()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Map<String, Object>>> listMine(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(playlistService.listMine(user.getUsername()));
    }

    @PostMapping("/{id}/tracks/{trackId}")
    public ResponseEntity<Map<String, Object>> addTrack(@PathVariable Long id, @PathVariable Long trackId,
                                                         @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(playlistService.addTrack(id, trackId, user.getUsername()));
    }

    @DeleteMapping("/{id}/tracks/{trackId}")
    public ResponseEntity<Map<String, Object>> removeTrack(@PathVariable Long id, @PathVariable Long trackId,
                                                            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(playlistService.removeTrack(id, trackId, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        playlistService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
