package com.soundkh.service;

import com.soundkh.dto.ChannelDto;
import com.soundkh.entity.Channel;
import com.soundkh.entity.User;
import com.soundkh.repository.ChannelRepository;
import com.soundkh.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock ChannelRepository channelRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ChannelService channelService;

    private User makeUser(String username) {
        var u = new User(); u.setUsername(username); u.setEmail(username + "@t.com");
        return u;
    }

    private Channel makeChannel(User owner, String name) {
        var c = new Channel(); c.setCreator(owner); c.setName(name);
        return c;
    }

    @Test
    void create_success() {
        var user = makeUser("chhay");
        when(userRepository.findByUsername("chhay")).thenReturn(Optional.of(user));
        when(channelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = channelService.create(new ChannelDto.CreateRequest("KH Music", "desc", null), "chhay");

        assertThat(res.name()).isEqualTo("KH Music");
        assertThat(res.creatorUsername()).isEqualTo("chhay");
    }

    @Test
    void listMine_returnsOwnChannels() {
        var user = makeUser("chhay");
        when(userRepository.findByUsername("chhay")).thenReturn(Optional.of(user));
        when(channelRepository.findByCreatorId(any())).thenReturn(
            List.of(makeChannel(user, "Ch1"), makeChannel(user, "Ch2"))
        );

        var res = channelService.listMine("chhay");
        assertThat(res).hasSize(2);
    }

    @Test
    void delete_notOwner_throws() {
        var owner = makeUser("chhay");
        var other = makeUser("other");
        var channel = makeChannel(owner, "Ch1");
        when(channelRepository.findById(1L)).thenReturn(Optional.of(channel));

        assertThatThrownBy(() -> channelService.delete(1L, "other"))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_owner_succeeds() {
        var owner = makeUser("chhay");
        var channel = makeChannel(owner, "Ch1");
        when(channelRepository.findById(1L)).thenReturn(Optional.of(channel));

        channelService.delete(1L, "chhay");
        verify(channelRepository).delete(channel);
    }
}
