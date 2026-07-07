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

public interface MessageRepository extends JpaRepository<Message, UUID> {

//    void createMessage(Message message);
//    Optional<Message> findMessageById(UUID id);
//    List<Message> findAllMessagesByChannelId(UUID channelId);
//    List<Message> findAllMessagesByUserId(UUID userId);
//    void save();
//    void deleteMessagesByChannelId(UUID channelId);
//    void deleteMessageById(UUID id);

    @EntityGraph(attributePaths = {"channel", "author", "attachments"})
    Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);

    @EntityGraph(attributePaths = {"channel", "author", "attachments"})
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.createdAt < :cursor")
    Slice<Message> findAllByChannelId(@Param("channelId") UUID channelId, @Param("cursor") Instant cursor, Pageable pageable);

    List<Message> findAllByChannelId(UUID channelId);
    List<Message> findAllByAuthorId(UUID authorId);
    void deleteAllByChannelId(UUID channelId);

}
