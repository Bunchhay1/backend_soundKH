package com.soundkh.service;

import com.soundkh.entity.Comment;
import com.soundkh.repository.CommentRepository;
import com.soundkh.repository.TrackRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, TrackRepository trackRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.trackRepository = trackRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> post(Long trackId, String content, String username) {
        var track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        var user = userRepository.findByUsername(username).orElseThrow();
        var comment = new Comment();
        comment.setTrack(track);
        comment.setUser(user);
        comment.setContent(content);
        var saved = commentRepository.save(comment);
        return toMap(saved);
    }

    public Page<Map<String, Object>> list(Long trackId, int page, int size) {
        return commentRepository.findByTrackIdOrderByCreatedAtDesc(trackId, PageRequest.of(page, size))
                .map(this::toMap);
    }

    public Map<String, Object> edit(Long commentId, String content, String username) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUser().getUsername().equals(username))
            throw new AccessDeniedException("Not your comment");
        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        return toMap(commentRepository.save(comment));
    }

    public void delete(Long commentId, String username) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUser().getUsername().equals(username))
            throw new AccessDeniedException("Not your comment");
        commentRepository.delete(comment);
    }

    private Map<String, Object> toMap(Comment c) {
        return Map.of(
                "id", c.getId(),
                "trackId", c.getTrack().getId(),
                "username", c.getUser().getUsername(),
                "content", c.getContent(),
                "createdAt", c.getCreatedAt().toString()
        );
    }
}
