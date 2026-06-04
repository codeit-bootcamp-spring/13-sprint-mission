package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {
    BinaryContent save(UUID authorID, UUID contentID);
    List<BinaryContent> findContantByAuthorID(UUID authorID);
    void delete(UUID authorID);
}
