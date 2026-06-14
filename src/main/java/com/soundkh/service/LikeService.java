package com.soundkh.service;

import com.soundkh.entity.Like;
import com.soundkh.repository.LikeRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, TrackRepository trackRepository,
                       UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> like(Long trackId, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        var user = userRepository.findByUsername(username).orElseThrow();
        if (likeRepository.existsByTrackIdAndUserId(trackId, user.getId()))
            throw new IllegalArgumentException("Already liked");
        var like = new Like();
        like.setTrack(track);
        like.setUser(user);
        likeRepository.save(like);
        return Map.of("trackId", trackId, "likes", likeRepository.countByTrackId(trackId));
    }

    public Map<String, Object> unlike(Long trackId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        likeRepository.findByTrackIdAndUserId(trackId, user.getId())
                .ifPresent(likeRepository::delete);
        return Map.of("trackId", trackId, "likes", likeRepository.countByTrackId(trackId));
    }

    public Map<String, Object> count(Long trackId) {
        return Map.of("trackId", trackId, "likes", likeRepository.countByTrackId(trackId));
    }
}
