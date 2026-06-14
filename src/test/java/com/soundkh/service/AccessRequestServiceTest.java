package com.soundkh.service;

import com.soundkh.entity.*;
import com.soundkh.repository.AccessRequestRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    @Mock AccessRequestRepository accessRequestRepository;
    @Mock TrackRepository trackRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @InjectMocks AccessRequestService accessRequestService;

    private User makeUser(String username) {
        var u = new User(); u.setUsername(username); return u;
    }

    private Track makeTrack(User owner) {
        var c = new Channel(); c.setCreator(owner); c.setName("Ch");
        var t = new Track(); t.setChannel(c); t.setTitle("Song");
        t.setS3ObjectKey("key"); t.setVisibility(Track.Visibility.PRIVATE);
        return t;
    }

    @Test
    void request_success() {
        var owner = makeUser("creator");
        var requester = makeUser("listener");
        var track = makeTrack(owner);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        when(userRepository.findByUsername("listener")).thenReturn(Optional.of(requester));
        when(accessRequestRepository.existsByTrackIdAndUserId(any(), any())).thenReturn(false);
        when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = accessRequestService.request(1L, "listener");
        assertThat(res.get("status")).isEqualTo("PENDING");
    }

    @Test
    void request_duplicate_throws() {
        var owner = makeUser("creator");
        var requester = makeUser("listener");
        var track = makeTrack(owner);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        when(userRepository.findByUsername("listener")).thenReturn(Optional.of(requester));
        when(accessRequestRepository.existsByTrackIdAndUserId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> accessRequestService.request(1L, "listener"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already submitted");
    }

    @Test
    void updateStatus_notOwner_throws() {
        var owner = makeUser("creator");
        var requester = makeUser("listener");
        var track = makeTrack(owner);
        var ar = new AccessRequest(); ar.setTrack(track); ar.setUser(requester);
        when(accessRequestRepository.findById(1L)).thenReturn(Optional.of(ar));

        assertThatThrownBy(() -> accessRequestService.updateStatus(1L, "APPROVED", "other"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_approve_success() {
        var owner = makeUser("creator");
        var requester = makeUser("listener");
        var track = makeTrack(owner);
        var ar = new AccessRequest(); ar.setTrack(track); ar.setUser(requester);
        when(accessRequestRepository.findById(1L)).thenReturn(Optional.of(ar));
        when(accessRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = accessRequestService.updateStatus(1L, "APPROVED", "creator");
        assertThat(res.get("status")).isEqualTo("APPROVED");
    }
}
