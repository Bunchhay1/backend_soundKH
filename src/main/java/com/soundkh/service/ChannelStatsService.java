package com.soundkh.service;

import com.soundkh.dto.ChannelDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class ChannelStatsService {

    @PersistenceContext
    private EntityManager em;

    public ChannelDto.StatsResponse getStats(Long channelId) {
        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT follower_count, total_plays, total_likes FROM channel_stats WHERE channel_id = :id")
                .setParameter("id", channelId)
                .getSingleResult();
        return new ChannelDto.StatsResponse(
                ((Number) row[0]).longValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue());
    }
}
