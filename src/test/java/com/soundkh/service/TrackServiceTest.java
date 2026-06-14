package com.soundkh.service;

import com.soundkh.dto.TrackDto;
import com.soundkh.entity.Channel;
import com.soundkh.entity.Track;
import com.soundkh.entity.User;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock TrackRepository trackRepository;
    @Mock ChannelRepository channelRepository;
    @Mock S3StorageService s3;
    @InjectMocks TrackService trackService;

    private User makeUser(String username) {
        var u = new User(); u.setUsername(username); return u;
    }

    private Channel makeChannel(User owner) {
        var c = new Channel(); c.setCreator(owner); c.setName("Ch"); return c;
    }

    private Track makeTrack(Channel channel, Track.Visibility vis) {
        var t = new Track(); t.setChannel(channel); t.setTitle("Song");
        t.setVisibility(vis); t.setS3ObjectKey("audio/1/file.mp3"); return t;
    }

    @Test
    void upload_success() throws IOException {
        var user = makeUser("chhay");
        var channel = makeChannel(user);
        when(channelRepository.findById(1L)).thenReturn(Optional.of(channel));
        when(trackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        var res = trackService.upload(1L, new TrackDto.UploadRequest("Song", null, Track.Visibility.PUBLIC), file, "chhay");

        assertThat(res.title()).isEqualTo("Song");
        verify(s3).uploadAudio(anyString(), any(), eq(3L));
    }

    @Test
    void upload_notOwner_throws() {
        var owner = makeUser("chhay");
        var channel = makeChannel(owner);
        when(channelRepository.findById(1L)).thenReturn(Optional.of(channel));

        var file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1});
        assertThatThrownBy(() ->
            trackService.upload(1L, new TrackDto.UploadRequest("Song", null, null), file, "other")
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void stream_privateTrack_throws() {
        var user = makeUser("chhay");
        var channel = makeChannel(user);
        var track = makeTrack(channel, Track.Visibility.PRIVATE);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        assertThatThrownBy(() -> trackService.stream(1L, null, null))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_notOwner_throws() {
        var owner = makeUser("chhay");
        var channel = makeChannel(owner);
        var track = makeTrack(channel, Track.Visibility.PUBLIC);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        assertThatThrownBy(() -> trackService.delete(1L, "other"))
            .isInstanceOf(AccessDeniedException.class);
    }
}
