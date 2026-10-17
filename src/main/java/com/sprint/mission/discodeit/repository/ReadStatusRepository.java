package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository
        extends JpaRepository<ReadStatus, UUID> {

    boolean existsByChannelIdAndUserId(
            UUID channelId,
            UUID userId
    );

    List<ReadStatus> findAllByUserId(UUID userId);

    List<ReadStatus> findAllByChannelId(UUID channelId);

    List<ReadStatus>
    findAllByChannelIdAndNotificationEnabledTrue(
            UUID channelId
    );

    void deleteByChannelId(UUID channelId);
}