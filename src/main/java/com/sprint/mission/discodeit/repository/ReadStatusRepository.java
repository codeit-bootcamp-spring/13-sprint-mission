package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;

public interface ReadStatusRepository {

    void create(ReadStatus readStatus);

    ReadStatus find(UUID id);

    List<ReadStatus> findAll();

    void delete(UUID id);

    List<ReadStatus> findByChannelId(UUID channelId);

    List<ReadStatus> findAllByUserId(UUID userId);

    ReadStatus findByUserIdAndChannelId(
            UUID userId,
            UUID channelId
    );

    void update(ReadStatus readStatus);
}