package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  Optional<ReadStatus> findByUser_IdAndChannel_Id(
      UUID userId,
      UUID channelId
  );

  List<ReadStatus> findByUser_Id(UUID userId);

  @EntityGraph(attributePaths = {"user"})
  List<ReadStatus> findByChannel_Id(UUID channelId);

  @EntityGraph(attributePaths = {"user", "channel"})
  List<ReadStatus> findByChannel_IdIn(List<UUID> channelIds);

  void deleteByChannel_Id(UUID channelId);
}

