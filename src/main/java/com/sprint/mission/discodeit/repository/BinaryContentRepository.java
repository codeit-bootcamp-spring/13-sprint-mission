package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    public BinaryContent save(UUID authorID, UUID contentID);
    public Optional<BinaryContent> findContantByAuthorID(UUID authorID);
    public void delete(UUID authorID);
}
