package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ReadStatusRepository {
    ReadStatus save(ReadStatus readStatus);
    List<ReadStatus> find(Predicate<ReadStatus> fn);
    void delete(UUID id);
}
