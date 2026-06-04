package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

    boolean existsReadStatusByUserIdAndChannelId(UUID userId, UUID channelId);
    void createReadStatus(ReadStatus readStatus);
    Optional<ReadStatus> findReadStatusById(UUID readStatusId);
    List<ReadStatus> findAllReadStatusByChannelId(UUID channelId);
    List<ReadStatus> findAllReadStatusByUserId(UUID userId);
    void save();
    void deleteReadStatusByChannelId(UUID channelId);
    void deleteReadStatusById(UUID readStatusId);
}
