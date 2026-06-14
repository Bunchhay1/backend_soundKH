package com.soundkh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracks")
public class Track {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String title;

    private String genre;
    private Integer duration;

    @Column(name = "s3_object_key", nullable = false)
    private String s3ObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "play_count", nullable = false)
    private long playCount = 0;

    @Column(columnDefinition = "TEXT")
    private String waveform;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Visibility { PUBLIC, PRIVATE }

    public Long getId() { return id; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel c) { this.channel = c; }
    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getGenre() { return genre; }
    public void setGenre(String g) { this.genre = g; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer d) { this.duration = d; }
    public String getS3ObjectKey() { return s3ObjectKey; }
    public void setS3ObjectKey(String k) { this.s3ObjectKey = k; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility v) { this.visibility = v; }
    public long getPlayCount() { return playCount; }
    public void setPlayCount(long p) { this.playCount = p; }
    public String getWaveform() { return waveform; }
    public void setWaveform(String w) { this.waveform = w; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
