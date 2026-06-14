package com.soundkh.service;

import com.soundkh.dto.ChannelDto;
import com.soundkh.entity.Channel;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ChannelFollowService channelFollowService;

    public ChannelService(ChannelRepository channelRepository, UserRepository userRepository,
                          ChannelFollowService channelFollowService) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.channelFollowService = channelFollowService;
    }

    public ChannelDto.Response create(ChannelDto.CreateRequest req, String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        var channel = new Channel();
        channel.setName(req.name());
        channel.setDescription(req.description());
        channel.setCreator(user);
        if (req.visibility() != null) channel.setVisibility(req.visibility());
        return toResponse(channelRepository.save(channel));
    }

    public List<ChannelDto.Response> listMine(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return channelRepository.findByCreatorId(user.getId()).stream().map(this::toResponse).toList();
    }

    public ChannelDto.Response get(Long id, String viewerUsername) {
        return toResponse(channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found")), viewerUsername);
    }

    public List<ChannelDto.Response> getFeatured() {
        return channelRepository.findByIsVerifiedTrue().stream().map(this::toResponse).toList();
    }

    public List<ChannelDto.Response> search(String q) {
        return channelRepository.searchByName(q).stream().map(this::toResponse).toList();
    }

    public ChannelDto.Response update(Long id, ChannelDto.UpdateRequest req, String username) {
        var channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        if (!channel.getCreator().getUsername().equals(username))
            throw new org.springframework.security.access.AccessDeniedException("Not your channel");
        if (req.name() != null) channel.setName(req.name());
        if (req.description() != null) channel.setDescription(req.description());
        return toResponse(channelRepository.save(channel));
    }

    public void delete(Long id, String username) {
        var channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        if (!channel.getCreator().getUsername().equals(username))
            throw new AccessDeniedException("Not your channel");
        channelRepository.delete(channel);
    }

    private ChannelDto.Response toResponse(Channel c) {
        return toResponse(c, null);
    }

    private ChannelDto.Response toResponse(Channel c, String viewerUsername) {
        long followers = channelFollowService.followerCount(c.getId());
        boolean following = viewerUsername != null && channelFollowService.isFollowing(c.getId(), viewerUsername);
        return new ChannelDto.Response(c.getId(), c.getName(), c.getDescription(),
                c.isVerified(), c.getCreator().getUsername(), c.getVisibility(), followers, following);
    }
}
