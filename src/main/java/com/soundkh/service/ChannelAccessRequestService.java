package com.soundkh.service;

import com.soundkh.entity.ChannelAccessRequest;
import com.soundkh.repository.ChannelAccessRequestRepository;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChannelAccessRequestService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ChannelAccessRequestRepository repo;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ChannelAccessRequestService(ChannelAccessRequestRepository repo,
                                       ChannelRepository channelRepository,
                                       UserRepository userRepository,
                                       EmailService emailService) {
        this.repo = repo;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> request(Long channelId, String username) {
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        var user = userRepository.findByUsername(username).orElseThrow();

        if (repo.existsByChannelIdAndUserId(channelId, user.getId()))
            throw new IllegalArgumentException("Request already submitted");

        var car = new ChannelAccessRequest();
        car.setChannel(channel);
        car.setUser(user);
        repo.save(car);
        return Map.of("channelId", channelId, "status", "PENDING");
    }

    public List<Map<String, Object>> listPending(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return repo.findByChannelCreatorId(user.getId()).stream()
                .filter(r -> r.getStatus() == ChannelAccessRequest.Status.PENDING)
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "channelId", r.getChannel().getId(),
                        "channelName", r.getChannel().getName(),
                        "requester", r.getUser().getUsername(),
                        "status", r.getStatus().name()))
                .toList();
    }

    public Map<String, Object> updateStatus(Long requestId, String status, String username) {
        var r = repo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!r.getChannel().getCreator().getUsername().equals(username))
            throw new AccessDeniedException("Not your channel");

        r.setStatus(ChannelAccessRequest.Status.valueOf(status.toUpperCase()));

        if (r.getStatus() == ChannelAccessRequest.Status.APPROVED) {
            r.setAccessCode(generateCode());
        }
        repo.save(r);

        emailService.sendAccessRequestStatus(
                r.getUser().getEmail(),
                r.getChannel().getName(),
                r.getStatus().name());

        var result = new HashMap<String, Object>();
        result.put("id", requestId);
        result.put("status", r.getStatus().name());
        if (r.getAccessCode() != null) result.put("accessCode", r.getAccessCode());
        return result;
    }

    public boolean hasChannelAccess(Long channelId, Long userId) {
        return repo.findByChannelIdAndUserId(channelId, userId)
                .map(r -> r.getStatus() == ChannelAccessRequest.Status.APPROVED)
                .orElse(false);
    }

    public java.util.List<Long> getApprovedChannelIds(Long userId) {
        return repo.findApprovedChannelIdsByUserId(userId);
    }

    private String generateCode() {
        var sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        return sb.toString();
    }
}
