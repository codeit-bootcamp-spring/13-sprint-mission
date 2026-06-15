package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface BinaryContentRepository {
    void save(BinaryContent bc);
    List<BinaryContent> find(Predicate<BinaryContent> fn);
    BinaryContent findByID(UUID id);
    List<BinaryContent> findByAuthorID(UUID userID);
    void delete(UUID id);
}
