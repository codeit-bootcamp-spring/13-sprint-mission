package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.stereotype.*;

import java.util.*;
@Repository
public interface ReadStatusRepository {

    void create(ReadStatus readStatus);

    ReadStatus find(UUID id);

    List<ReadStatus> findAll();

    void delete(UUID id);

    List<ReadStatus> findByChannelId(UUID channelId);

    ReadStatus findByUserIdAndChannelId(
            UUID userId,
            UUID channelId
    );
}