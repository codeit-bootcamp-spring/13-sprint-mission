package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.UUID;

public interface ReadStatusRepository {

    void create(ReadStatus readStatus);
    void deleteByChannelId(UUID channelId);

}
