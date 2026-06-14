package com.soundkh.controller;

import com.soundkh.dto.ChannelDto;
import com.soundkh.service.ChannelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping
    public ResponseEntity<ChannelDto.Response> create(@Valid @RequestBody ChannelDto.CreateRequest req,
                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(channelService.create(req, user.getUsername()));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ChannelDto.Response>> featured() {
        return ResponseEntity.ok(channelService.getFeatured());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ChannelDto.Response>> listMine(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(channelService.listMine(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelDto.Response> get(@PathVariable Long id) {
        return ResponseEntity.ok(channelService.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChannelDto.Response>> search(@RequestParam String q) {
        return ResponseEntity.ok(channelService.search(q));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChannelDto.Response> update(@PathVariable Long id,
                                                       @Valid @RequestBody ChannelDto.UpdateRequest req,
                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(channelService.update(id, req, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        channelService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
