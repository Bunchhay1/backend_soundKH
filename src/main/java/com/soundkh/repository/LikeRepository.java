package com.soundkh.repository;

import com.soundkh.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByTrackIdAndUserId(Long trackId, Long userId);
    long countByTrackId(Long trackId);
    boolean existsByTrackIdAndUserId(Long trackId, Long userId);
}
