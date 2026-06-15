package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ReadStatusRepository {
    void save(ReadStatus readStatus);
    List<ReadStatus> find(Predicate<ReadStatus> fn);
    ReadStatus findByID(UUID id);
    List<ReadStatus> findbyChennalID(UUID id);
    void delete(UUID id);
}
