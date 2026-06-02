package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public interface BinaryContentRepository {
    public BinaryContent create();
    public BinaryContent getContantByAuthorID(UUID id);
    public void delete(UUID id);
}
