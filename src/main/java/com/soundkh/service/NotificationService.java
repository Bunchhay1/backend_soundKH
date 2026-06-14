package com.soundkh.service;

import com.soundkh.entity.Notification;
import com.soundkh.repository.NotificationRepository;
import com.soundkh.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> list(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toMap).toList();
    }

    public Map<String, Object> markRead(Long id, String username) {
        var n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getUser().getUsername().equals(username))
            throw new org.springframework.security.access.AccessDeniedException("Not your notification");
        n.setRead(true);
        return toMap(notificationRepository.save(n));
    }

    @Transactional
    public void clearAll(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        notificationRepository.deleteAllByUserId(user.getId());
    }

    /** Called internally to push a notification to a user. */
    public void push(com.soundkh.entity.User user, String type, String message) {
        var n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    private Map<String, Object> toMap(Notification n) {
        return Map.of(
                "id", n.getId(),
                "type", n.getType(),
                "message", n.getMessage(),
                "isRead", n.isRead(),
                "createdAt", n.getCreatedAt().toString()
        );
    }
}
