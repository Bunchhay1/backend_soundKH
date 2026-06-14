package com.soundkh.service;

import com.soundkh.dto.TrackDto;
import com.soundkh.entity.AccessRequest;
import com.soundkh.entity.Track;
import com.soundkh.entity.User;
import com.soundkh.repository.AccessRequestRepository;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.LikeRepository;
import com.soundkh.repository.SubscriptionRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TrackService {

    private final TrackRepository trackRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final S3StorageService s3;
    private final TranscodingService transcodingService;
    private final SubscriptionRepository subscriptionRepository;
    private final ChannelAccessRequestService channelAccessRequestService;
    private final LikeRepository likeRepository;
    private final ChannelManagerService channelManagerService;

    public TrackService(TrackRepository trackRepository, ChannelRepository channelRepository,
                        UserRepository userRepository, AccessRequestRepository accessRequestRepository,
                        S3StorageService s3, TranscodingService transcodingService,
                        SubscriptionRepository subscriptionRepository,
                        ChannelAccessRequestService channelAccessRequestService,
                        LikeRepository likeRepository,
                        ChannelManagerService channelManagerService) {
        this.trackRepository = trackRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.s3 = s3;
        this.transcodingService = transcodingService;
        this.subscriptionRepository = subscriptionRepository;
        this.channelAccessRequestService = channelAccessRequestService;
        this.likeRepository = likeRepository;
        this.channelManagerService = channelManagerService;
    }

    // Phase 6: Upload with genre
    @CacheEvict(value = "tracks", key = "#channelId")
    public TrackDto.Response upload(Long channelId, TrackDto.UploadRequest req,
                                    MultipartFile file, String username) throws IOException {
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        if (!channel.getCreator().getUsername().equals(username))
            throw new AccessDeniedException("Not your channel");

        // SuperStar tier check: only SUPER_STAR or ADMIN may upload
        var uploader = userRepository.findByUsername(username).orElseThrow();
        if (uploader.getRole() != User.Role.SUPER_STAR && uploader.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("SuperStar subscription required to upload tracks");
        }

        String key = "audio/" + channelId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3.uploadAudio(key, file.getInputStream(), file.getSize());

        var track = new Track();
        track.setChannel(channel);
        track.setTitle(req.title());
        track.setGenre(req.genre());
        track.setVisibility(req.visibility() != null ? req.visibility() : Track.Visibility.PRIVATE);
        track.setS3ObjectKey(key);
        var saved = trackRepository.save(track);
        // Phase 16-17: async transcoding + waveform generation
        transcodingService.transcodeAsync(saved.getId(), key);
        transcodingService.generateWaveformAsync(saved.getId(), key);
        return toResponse(saved);
    }

    // Phase 11: Pagination + genre filter
    public Page<TrackDto.Response> listByChannel(Long channelId, int page, int size, String genre) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Track> result = (genre != null && !genre.isBlank())
                ? trackRepository.findByChannelIdAndGenre(channelId, genre, pageable)
                : trackRepository.findByChannelId(channelId, pageable);
        return result.map(this::toResponse);
    }

    // Phase 12: Full-text search
    public Page<TrackDto.Response> search(String q, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return trackRepository.search(q, pageable).map(this::toResponse);
    }

    // Phase 7: Byte-range streaming + Phase 8: access check + play_count
    @Transactional
    public StreamResult stream(Long trackId, String rangeHeader, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        if (track.getVisibility() == Track.Visibility.PRIVATE) {
            if (username == null) throw new AccessDeniedException("Login required");
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AccessDeniedException("User not found"));
            boolean isOwner = track.getChannel().getCreator().getUsername().equals(username);
            if (!isOwner) {
                boolean trackApproved = accessRequestRepository
                        .findByTrackIdAndUserId(trackId, user.getId())
                        .map(ar -> ar.getStatus() == AccessRequest.Status.APPROVED)
                        .orElse(false);
                boolean channelApproved = channelAccessRequestService
                        .hasChannelAccess(track.getChannel().getId(), user.getId());
                if (!trackApproved && !channelApproved)
                    throw new AccessDeniedException("Access not granted");
            }
        }

        trackRepository.incrementPlayCount(trackId);
        long totalSize = s3.getObjectSize(track.getS3ObjectKey());

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(parts[0]);
            long end = parts.length > 1 && !parts[1].isBlank()
                    ? Long.parseLong(parts[1]) : Math.min(start + 1048576L, totalSize - 1);
            long length = end - start + 1;
            var stream = s3.downloadRange(track.getS3ObjectKey(), rangeHeader);
            return new StreamResult(stream, start, end, totalSize, length, true);
        }

        return new StreamResult(s3.downloadAudio(track.getS3ObjectKey()), 0, totalSize - 1, totalSize, totalSize, false);
    }

    // Phase 10: Presigned URL
    public TrackDto.PresignedResponse presign(Long trackId, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        if (track.getVisibility() == Track.Visibility.PRIVATE) {
            if (username == null) throw new AccessDeniedException("Login required");
            boolean isOwner = track.getChannel().getCreator().getUsername().equals(username);
            if (!isOwner) {
                var user = userRepository.findByUsername(username).orElseThrow();
                boolean trackApproved = accessRequestRepository
                        .findByTrackIdAndUserId(trackId, user.getId())
                        .map(ar -> ar.getStatus() == AccessRequest.Status.APPROVED)
                        .orElse(false);
                boolean channelApproved = channelAccessRequestService
                        .hasChannelAccess(track.getChannel().getId(), user.getId());
                if (!trackApproved && !channelApproved)
                    throw new AccessDeniedException("Access not granted");
            }
        }
        Duration ttl = Duration.ofMinutes(15);
        return new TrackDto.PresignedResponse(s3.presign(track.getS3ObjectKey(), ttl), ttl.toSeconds());
    }

    @CacheEvict(value = "tracks", allEntries = true)
    public TrackDto.Response update(Long trackId, TrackDto.UpdateRequest req, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        boolean isOwner = track.getChannel().getCreator().getUsername().equals(username);
        boolean isManager = channelManagerService.isManager(track.getChannel().getId(), username);
        if (!isOwner && !isManager) throw new AccessDeniedException("Not authorized");
        if (req.title() != null) track.setTitle(req.title());
        if (req.genre() != null) track.setGenre(req.genre());
        if (req.visibility() != null) track.setVisibility(req.visibility());
        return toResponse(trackRepository.save(track));
    }

    public Page<TrackDto.Response> trending(int page, int size) {
        var since = LocalDateTime.now().minusDays(7);
        return trackRepository.findTrending(since, PageRequest.of(page, size)).map(this::toResponse);
    }

    public Page<TrackDto.Response> newReleases(int page, int size) {
        return trackRepository.findByVisibility(Track.Visibility.PUBLIC,
                PageRequest.of(page, size, Sort.by("createdAt").descending())).map(this::toResponse);
    }

    public Page<TrackDto.Response> feed(String username, int page, int size) {
        var user = userRepository.findByUsername(username).orElseThrow();
        var channelIds = channelAccessRequestService.getApprovedChannelIds(user.getId());
        if (channelIds.isEmpty()) return Page.empty();
        return trackRepository.findFeed(channelIds, PageRequest.of(page, size)).map(this::toResponse);
    }

    @CacheEvict(value = "tracks", allEntries = true)
    public void delete(Long trackId, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        boolean isOwner = track.getChannel().getCreator().getUsername().equals(username);
        boolean isManager = channelManagerService.isManager(track.getChannel().getId(), username);
        if (!isOwner && !isManager) throw new AccessDeniedException("Not authorized");
        s3.deleteAudio(track.getS3ObjectKey());
        trackRepository.delete(track);
    }

    private TrackDto.Response toResponse(Track t) {
        long likeCount = likeRepository.countByTrackId(t.getId());
        return new TrackDto.Response(t.getId(), t.getTitle(), t.getGenre(),
                t.getDuration(), t.getVisibility().name(), t.getPlayCount(), likeCount,
                t.getChannel().getId(), t.getChannel().getName());
    }

    public record StreamResult(
        InputStream stream, long start, long end, long totalSize, long length, boolean isPartial
    ) {}
}
