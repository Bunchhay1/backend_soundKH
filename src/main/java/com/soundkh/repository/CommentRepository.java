package com.soundkh.repository;

import com.soundkh.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByTrackIdOrderByCreatedAtDesc(Long trackId, Pageable pageable);
}
