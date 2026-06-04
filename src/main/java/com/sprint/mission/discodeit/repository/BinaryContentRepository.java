package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface BinaryContentRepository {
    BinaryContent save(BinaryContent bc);
    List<BinaryContent> find(Predicate<BinaryContent> fn);
    BinaryContent findByID(UUID id);
    void update(BinaryContent bc);
    void delete(UUID authorID);
}
