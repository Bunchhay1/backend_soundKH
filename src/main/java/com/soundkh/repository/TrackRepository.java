package com.soundkh.repository;

import com.soundkh.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TrackRepository extends JpaRepository<Track, Long> {

    Page<Track> findByChannelId(Long channelId, Pageable pageable);
    Page<Track> findByChannelIdAndVisibility(Long channelId, Track.Visibility visibility, Pageable pageable);
    Page<Track> findByChannelIdAndGenre(Long channelId, String genre, Pageable pageable);

    @Query(value = "SELECT * FROM tracks WHERE search_vector @@ plainto_tsquery('english',:q) AND visibility='PUBLIC' ORDER BY play_count DESC",
           countQuery = "SELECT count(*) FROM tracks WHERE search_vector @@ plainto_tsquery('english',:q) AND visibility='PUBLIC'",
           nativeQuery = true)
    Page<Track> search(@Param("q") String q, Pageable pageable);

    @Modifying
    @Query("UPDATE Track t SET t.playCount = t.playCount + 1 WHERE t.id = :id")
    void incrementPlayCount(@Param("id") Long id);

    // Trending: public tracks ordered by play_count in last 7 days
    @Query("SELECT t FROM Track t WHERE t.visibility = 'PUBLIC' AND t.createdAt >= :since ORDER BY t.playCount DESC")
    Page<Track> findTrending(@Param("since") LocalDateTime since, Pageable pageable);

    // New releases: latest public tracks
    Page<Track> findByVisibility(Track.Visibility visibility, Pageable pageable);

    // Feed: public tracks from channels the user follows (approved channel access requests)
    @Query("SELECT t FROM Track t WHERE t.channel.id IN :channelIds AND t.visibility = 'PUBLIC' ORDER BY t.createdAt DESC")
    Page<Track> findFeed(@Param("channelIds") java.util.List<Long> channelIds, Pageable pageable);
}
