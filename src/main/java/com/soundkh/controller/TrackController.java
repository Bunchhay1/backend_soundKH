package com.soundkh.controller;

import com.soundkh.dto.TrackDto;
import com.soundkh.entity.Track;
import com.soundkh.service.TrackService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    // Phase 6: Upload with genre
    @PostMapping("/channels/{channelId}")
    public ResponseEntity<TrackDto.Response> upload(
            @PathVariable Long channelId,
            @RequestParam String title,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "PRIVATE") Track.Visibility visibility,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal UserDetails user) throws IOException {
        return ResponseEntity.ok(trackService.upload(channelId,
                new TrackDto.UploadRequest(title, genre, visibility), file, user.getUsername()));
    }

    // Phase 11: Paginated list with optional genre filter
    @GetMapping("/channels/{channelId}")
    public ResponseEntity<Page<TrackDto.Response>> listByChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String genre) {
        return ResponseEntity.ok(trackService.listByChannel(channelId, page, size, genre));
    }

    // Phase 12: Full-text search
    @GetMapping("/search")
    public ResponseEntity<Page<TrackDto.Response>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.search(q, page, size));
    }

    // Phase 7: Byte-range streaming
    @GetMapping("/{id}/stream")
    public ResponseEntity<InputStreamResource> stream(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : null;
        var result = trackService.stream(id, range, username);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentLength(result.length());
        headers.set(HttpHeaders.CONTENT_RANGE,
                "bytes " + result.start() + "-" + result.end() + "/" + result.totalSize());

        HttpStatus status = result.isPartial() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return ResponseEntity.status(status).headers(headers)
                .body(new InputStreamResource(result.stream()));
    }

    // Phase 10: Presigned URL
    @GetMapping("/{id}/presign")
    public ResponseEntity<TrackDto.PresignedResponse> presign(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        String username = user != null ? user.getUsername() : null;
        return ResponseEntity.ok(trackService.presign(id, username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackDto.Response> update(
            @PathVariable Long id,
            @RequestBody TrackDto.UpdateRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(trackService.update(id, req, user.getUsername()));
    }

    @GetMapping("/trending")
    public ResponseEntity<Page<TrackDto.Response>> trending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.trending(page, size));
    }

    @GetMapping("/new-releases")
    public ResponseEntity<Page<TrackDto.Response>> newReleases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.newReleases(page, size));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<TrackDto.Response>> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(trackService.feed(user.getUsername(), page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        trackService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
