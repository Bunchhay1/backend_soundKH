package com.soundkh.service;

import com.soundkh.entity.ChannelFollow;
import com.soundkh.repository.ChannelFollowRepository;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ChannelFollowService {

    private final ChannelFollowRepository followRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public ChannelFollowService(ChannelFollowRepository followRepository,
                                 ChannelRepository channelRepository,
                                 UserRepository userRepository) {
        this.followRepository = followRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    public void follow(Long channelId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        if (followRepository.existsByChannelIdAndUserId(channelId, user.getId())) return;
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        var f = new ChannelFollow();
        f.setChannel(channel);
        f.setUser(user);
        followRepository.save(f);
    }

    public void unfollow(Long channelId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        followRepository.findByChannelIdAndUserId(channelId, user.getId())
                .ifPresent(followRepository::delete);
    }

    public long followerCount(Long channelId) {
        return followRepository.countByChannelId(channelId);
    }

    public boolean isFollowing(Long channelId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return followRepository.existsByChannelIdAndUserId(channelId, user.getId());
    }
}
