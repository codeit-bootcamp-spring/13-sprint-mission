package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatus save(ReadStatus readStatus);

    ReadStatus findById(UUID id);

    List<ReadStatus> findAll();

    void delete(UUID id);

    List<ReadStatus> findAllByChannelId(UUID channelId);

    List<ReadStatus> findAllByUserId(UUID userId);

    ReadStatus findByUserIdAndChannelId(UUID userId, UUID channelId);

}
