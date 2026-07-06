package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JPAMessageRepository extends JpaRepository<Message, UUID> {
    @Query(value = "SELECT * FROM messages WHERE channel_id = ? ORDER BY created_at DESC",nativeQuery = true)
    List<Message> findByChannelId(@Param("channelId") UUID channelId);
}