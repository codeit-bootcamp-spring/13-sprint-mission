package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId, Pageable pageable);

    @Query("select msg.id from Message msg where msg.channel.id = :id and msg.createdAt <= :ctime order by msg.createdAt desc")
    Slice<UUID> findMessageIdsByChannelIdWithCursor(
            @Param("id") UUID id,
            Pageable pageable,
            @Param("ctime") Instant ctime
    );
}