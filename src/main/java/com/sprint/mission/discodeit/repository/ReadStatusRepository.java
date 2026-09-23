package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  Optional<ReadStatus> findByUserIdAndChannelId(
      UUID userId,
      UUID channelId
  );

  List<ReadStatus> findByUserId(UUID userId);

  @EntityGraph(attributePaths = {"user"})
  List<ReadStatus> findByChannelId(UUID channelId);

  @EntityGraph(attributePaths = {"user", "channel"})
  List<ReadStatus> findByChannelIdIn(List<UUID> channelIds);

  @EntityGraph(attributePaths = {"channel"})
  List<ReadStatus> findByUserIdAndChannelType(
      UUID userId,
      ChannelType channelType
  );

  void deleteByChannelId(UUID channelId);
}

