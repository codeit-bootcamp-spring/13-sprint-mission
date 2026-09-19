package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  boolean existsByIdAndAuthorId(UUID messageId, UUID authorId);

  List<Message> findAllByChannelId(UUID channelId);

  @EntityGraph(attributePaths = {"author", "author.profile"})
  Slice<Message> findAllByChannelId(UUID channelId, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "author.profile"})
  Slice<Message> findAllByChannelIdAndCreatedAtLessThan(
      UUID channelId, Instant cursor, Pageable pageable);

  long countByChannelId(UUID channelId);

}
