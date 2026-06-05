package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Collection;
import java.util.UUID;

public interface BinaryContentRepository {
    void save(BinaryContent binaryContent);
    BinaryContent findById(UUID id);
    Collection<BinaryContent> findAllByIdIn(UUID ids);
    void delete(UUID id);
}
