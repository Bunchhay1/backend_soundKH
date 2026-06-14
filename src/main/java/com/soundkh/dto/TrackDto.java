package com.soundkh.dto;

import com.soundkh.entity.Track;
import jakarta.validation.constraints.NotBlank;

public class TrackDto {

    public record UploadRequest(
        @NotBlank String title,
        String genre,
        Track.Visibility visibility
    ) {}

    public record UpdateRequest(String title, String genre, Track.Visibility visibility) {}

    public record Response(
        Long id, String title, String genre,
        Integer duration, String visibility,
        long playCount, Long channelId
    ) {}

    public record PresignedResponse(String url, long expiresInSeconds) {}
}
