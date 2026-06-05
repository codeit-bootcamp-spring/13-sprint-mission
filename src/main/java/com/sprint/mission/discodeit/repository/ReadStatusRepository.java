package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.Collection;
import java.util.UUID;

public interface ReadStatusRepository {
    void save(ReadStatus readStatus);
    ReadStatus findById(UUID id );
    Collection<ReadStatus> findByAllUserId(UUID userId);
    void delete(UUID userid);




}
