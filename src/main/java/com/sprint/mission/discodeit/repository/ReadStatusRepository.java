package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ReadStatusRepository {
    public ReadStatus save(ReadStatus readStatus);
    public List<ReadStatus> find(Predicate<ReadStatus> fn);
    public void delete(UUID id);
}
