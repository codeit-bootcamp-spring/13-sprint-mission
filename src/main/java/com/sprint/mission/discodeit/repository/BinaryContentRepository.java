package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public interface BinaryContentRepository {

    BinaryContent findByUserId(UUID userId);

    void delete(UUID id);

}
