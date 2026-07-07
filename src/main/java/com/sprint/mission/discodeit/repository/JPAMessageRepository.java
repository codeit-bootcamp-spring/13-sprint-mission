package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JPAMessageRepository extends JpaRepository<Message, UUID> {
    @Query(value = "SELECT * FROM messages WHERE channel_id = ? ORDER BY created_at DESC",nativeQuery = true)
    List<Message> findByChannelId(@Param("channelId") UUID channelId);

    Slice<Message> findByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId, Pageable pageable);

    @EntityGraph(attributePaths = {"author","attachment"})
    @Query("SELECT m FROM Message m WHERE m.channel.id = :id")
    Slice<Message> findByChannelIdForMessageDto(@Param("id")UUID channelId, Pageable pageable);

    @EntityGraph(attributePaths = {"author","attachment"})
    @Query("SELECT m FROM Message m WHERE m.channel.id = :id AND m.createdAt < :ctime")
    Slice<Message> findByChannelWithCursor(@Param("id") UUID channelId, Pageable pageable,@Param("ctime") Instant ctime);

    // 페이징에 걸리는건지, sql 행이 리밋(페이징 기준)이 걸리는 건지 메세지를 원래대로 쿼리하지 못하는 상황이 발생.

//    @Query(
//"""
//SELECT m FROM Message m
//JOIN FETCH m.author u
//JOIN FETCH u.status us
//JOIN FETCH m.attachment att
//JOIN FETCH m.channel c
//WHERE c.id = :chn
//""")
//    Slice<Message> findByChannelIdForMessageDto(@Param("chn")UUID channelId, Pageable pageable);
}