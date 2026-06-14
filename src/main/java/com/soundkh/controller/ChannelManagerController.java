package com.soundkh.controller;

import com.soundkh.service.ChannelManagerService;
import com.soundkh.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels/{channelId}/managers")
public class ChannelManagerController {

    private final ChannelManagerService managerService;
    private final ChannelService channelService;

    public ChannelManagerController(ChannelManagerService managerService, ChannelService channelService) {
        this.managerService = managerService;
        this.channelService = channelService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Void> assign(@PathVariable Long channelId, @PathVariable Long userId,
                                       @AuthenticationPrincipal UserDetails user) {
        managerService.assignByOwner(channelId, userId, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable Long channelId, @PathVariable Long userId,
                                       @AuthenticationPrincipal UserDetails user) {
        managerService.removeByOwner(channelId, userId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Map<String, List<String>>> list(@PathVariable Long channelId) {
        return ResponseEntity.ok(Map.of("managers", managerService.listManagers(channelId)));
    }
}
