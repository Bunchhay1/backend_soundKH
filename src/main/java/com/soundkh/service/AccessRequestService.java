package com.soundkh.service;

import com.soundkh.entity.AccessRequest;
import com.soundkh.repository.AccessRequestRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AccessRequestService(AccessRequestRepository accessRequestRepository,
                                TrackRepository trackRepository, UserRepository userRepository,
                                EmailService emailService) {
        this.accessRequestRepository = accessRequestRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> request(Long trackId, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        var user = userRepository.findByUsername(username).orElseThrow();

        if (accessRequestRepository.existsByTrackIdAndUserId(trackId, user.getId()))
            throw new IllegalArgumentException("Request already submitted");

        var ar = new AccessRequest();
        ar.setTrack(track);
        ar.setUser(user);
        accessRequestRepository.save(ar);
        return Map.of("trackId", trackId, "status", "PENDING");
    }

    public List<Map<String, Object>> listPending(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return accessRequestRepository.findByTrackChannelCreatorId(user.getId()).stream()
                .filter(ar -> ar.getStatus() == AccessRequest.Status.PENDING)
                .map(ar -> Map.<String, Object>of(
                        "id", ar.getId(),
                        "trackId", ar.getTrack().getId(),
                        "requester", ar.getUser().getUsername(),
                        "status", ar.getStatus().name()))
                .toList();
    }

    // Phase 13: sends email after status update
    public Map<String, Object> updateStatus(Long requestId, String status, String username) {
        var ar = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!ar.getTrack().getChannel().getCreator().getUsername().equals(username))
            throw new AccessDeniedException("Not your track");

        ar.setStatus(AccessRequest.Status.valueOf(status.toUpperCase()));
        accessRequestRepository.save(ar);

        // Phase 13: async email notification
        emailService.sendAccessRequestStatus(
                ar.getUser().getEmail(),
                ar.getTrack().getTitle(),
                ar.getStatus().name());

        return Map.of("id", requestId, "status", ar.getStatus().name());
    }
}
