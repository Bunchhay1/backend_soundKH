package com.soundkh.repository;

import com.soundkh.entity.ChannelManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelManagerRepository extends JpaRepository<ChannelManager, Long> {
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);
    Optional<ChannelManager> findByChannelIdAndUserId(Long channelId, Long userId);
    List<ChannelManager> findByChannelId(Long channelId);
}
