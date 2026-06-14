package com.soundkh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_access_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"}))
public class ChannelAccessRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "access_code", length = 8)
    private String accessCode;

    public enum Status { PENDING, APPROVED, REJECTED }

    public Long getId() { return id; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel c) { this.channel = c; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getAccessCode() { return accessCode; }
    public void setAccessCode(String code) { this.accessCode = code; }
}
