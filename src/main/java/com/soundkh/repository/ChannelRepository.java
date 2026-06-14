package com.soundkh.repository;

import com.soundkh.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByCreatorId(Long creatorId);
    List<Channel> findByIsVerifiedTrue();

    @Query("SELECT c FROM Channel c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Channel> searchByName(@Param("q") String q);
}
