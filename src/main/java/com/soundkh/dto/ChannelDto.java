package com.soundkh.dto;

import com.soundkh.entity.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChannelDto {

    public record CreateRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        Channel.Visibility visibility
    ) {}

    public record UpdateRequest(
        @Size(max = 100) String name,
        String description
    ) {}

    public record Response(Long id, String name, String description,
                           boolean isVerified, String creatorUsername,
                           Channel.Visibility visibility,
                           long followerCount,
                           boolean isFollowing) {}

    public record StatsResponse(long followerCount, long totalPlays, long totalLikes) {}
}
