package com.soundkh.repository;

import com.soundkh.entity.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    Optional<AccessRequest> findByTrackIdAndUserId(Long trackId, Long userId);
    List<AccessRequest> findByTrackChannelCreatorId(Long creatorId);
    boolean existsByTrackIdAndUserId(Long trackId, Long userId);
}
