package com.soundkh.repository;

import com.soundkh.entity.ChannelAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChannelAccessRequestRepository extends JpaRepository<ChannelAccessRequest, Long> {
    Optional<ChannelAccessRequest> findByChannelIdAndUserId(Long channelId, Long userId);
    List<ChannelAccessRequest> findByChannelCreatorId(Long creatorId);
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT r.channel.id FROM ChannelAccessRequest r WHERE r.user.id = :userId AND r.status = 'APPROVED'")
    List<Long> findApprovedChannelIdsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
