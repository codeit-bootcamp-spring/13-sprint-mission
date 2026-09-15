package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);
    Slice<Message> findAllByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    Slice<Message> findAllByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(UUID channelId, Instant cursor, Pageable pageable);

    // MessageRepository
    @Modifying
    @Query("DELETE FROM Message m WHERE m.channel.id = :channelId")
    void deleteByChannelId(@Param("channelId") UUID channelId);
}
