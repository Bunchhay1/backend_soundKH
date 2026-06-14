package com.soundkh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_follows", uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"}))
public class ChannelFollow {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel c) { this.channel = c; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
}
