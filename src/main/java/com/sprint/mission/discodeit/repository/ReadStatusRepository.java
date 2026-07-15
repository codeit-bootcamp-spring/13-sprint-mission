package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

    @EntityGraph(attributePaths = {
            "channel"
    })
    Collection<ReadStatus> findAllByUser_Id(UUID userId);

    Optional<ReadStatus> findByUser_IdAndChannel_Id(UUID userId, UUID channelId);

    @EntityGraph(attributePaths = {
            "user",
            "user.profile"
    })
    Collection<ReadStatus> findAllByChannel_Id(UUID channelId);
}