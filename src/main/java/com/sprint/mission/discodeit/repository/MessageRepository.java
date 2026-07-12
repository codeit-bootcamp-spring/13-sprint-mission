package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Collection<Message> findAllByChannel_Id(UUID channelId);

    @EntityGraph(attributePaths = {
            "author",
            "author.profile",
            "attachments",
            "channel"
    })
    Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "author",
            "author.profile",
            "attachments",
            "channel"
    })
    Slice<Message> findAllByChannel_IdAndCreatedAtLessThan(
            UUID channelId,
            Instant cursor,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "author",
            "author.profile",
            "attachments",
            "channel"
    })
    Optional<Message> findWithDetailsById(UUID id);

    Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);

}