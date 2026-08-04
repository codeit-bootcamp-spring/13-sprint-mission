package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

    @EntityGraph(attributePaths = {"user", "channel"})
    List<ReadStatus> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"user"})
    List<ReadStatus> findAllByChannelId(UUID channelId);
    void deleteAllByChannelId(UUID channelId);

}
