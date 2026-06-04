package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {

    List<ReadStatus> findAllReadStatusByChannelId(UUID channelId);
    List<ReadStatus> findAllReadStatusByUserId(UUID userId);
    void deleteReadStatusByChannelId(UUID channelId);
}
