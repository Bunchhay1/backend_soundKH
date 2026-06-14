package com.soundkh.service;

import com.soundkh.entity.Subscription;
import com.soundkh.entity.User;
import com.soundkh.repository.SubscriptionRepository;
import com.soundkh.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    /** Activate or renew SuperStar for 30 days. In production, call this after payment confirmation. */
    @Transactional
    public Map<String, Object> activate(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();

        var sub = subscriptionRepository.findByUserId(user.getId()).orElseGet(() -> {
            var s = new Subscription();
            s.setUser(user);
            s.setPlan(Subscription.Plan.SUPER_STAR);
            return s;
        });

        // Extend from now or from current expiry if still active
        LocalDateTime base = (sub.getExpiresAt() != null && sub.isActive())
                ? sub.getExpiresAt() : LocalDateTime.now();
        sub.setExpiresAt(base.plusDays(30));
        subscriptionRepository.save(sub);

        // Upgrade role
        user.setRole(User.Role.SUPER_STAR);
        userRepository.save(user);

        return Map.of("username", username, "plan", "SUPER_STAR", "expiresAt", sub.getExpiresAt().toString());
    }

    public Map<String, Object> status(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        return subscriptionRepository.findByUserId(user.getId())
                .map(s -> Map.<String, Object>of(
                        "plan", s.getPlan().name(),
                        "active", s.isActive(),
                        "expiresAt", s.getExpiresAt().toString()))
                .orElse(Map.of("plan", "NONE", "active", false));
    }
}
