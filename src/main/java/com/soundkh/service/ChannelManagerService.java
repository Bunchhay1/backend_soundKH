package com.soundkh.service;

import com.soundkh.entity.ChannelManager;
import com.soundkh.repository.ChannelManagerRepository;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelManagerService {

    private final ChannelManagerRepository managerRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public ChannelManagerService(ChannelManagerRepository managerRepository,
                                  ChannelRepository channelRepository,
                                  UserRepository userRepository) {
        this.managerRepository = managerRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }

    /** Owner or Admin assigns a user as channel manager */
    public void assignByOwner(Long channelId, Long userId, String callerUsername) {
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        var caller = userRepository.findByUsername(callerUsername).orElseThrow();
        boolean isOwner = channel.getCreator().getUsername().equals(callerUsername);
        boolean isAdmin = caller.getRole() == com.soundkh.entity.User.Role.ADMIN;
        if (!isOwner && !isAdmin) throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        assign(channelId, userId);
    }

    /** Owner or Admin removes a channel manager */
    public void removeByOwner(Long channelId, Long userId, String callerUsername) {
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        var caller = userRepository.findByUsername(callerUsername).orElseThrow();
        boolean isOwner = channel.getCreator().getUsername().equals(callerUsername);
        boolean isAdmin = caller.getRole() == com.soundkh.entity.User.Role.ADMIN;
        if (!isOwner && !isAdmin) throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        remove(channelId, userId);
    }

    /** Admin assigns a user as channel manager */
    public void assign(Long channelId, Long userId) {
        if (managerRepository.existsByChannelIdAndUserId(channelId, userId)) return;
        var channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var m = new ChannelManager();
        m.setChannel(channel);
        m.setUser(user);
        managerRepository.save(m);
    }

    /** Admin removes a channel manager */
    public void remove(Long channelId, Long userId) {
        managerRepository.findByChannelIdAndUserId(channelId, userId)
                .ifPresent(managerRepository::delete);
    }

    public boolean isManager(Long channelId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return managerRepository.existsByChannelIdAndUserId(channelId, user.getId());
    }

    public List<String> listManagers(Long channelId) {
        return managerRepository.findByChannelId(channelId).stream()
                .map(m -> m.getUser().getUsername())
                .toList();
    }
}
