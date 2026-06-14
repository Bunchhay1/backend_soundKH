package com.soundkh.repository;

import com.soundkh.entity.ChannelFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelFollowRepository extends JpaRepository<ChannelFollow, Long> {
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);
    Optional<ChannelFollow> findByChannelIdAndUserId(Long channelId, Long userId);
    long countByChannelId(Long channelId);
}
