package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {

    List<BinaryContent> findAllByMessageId(UUID messageId);

    BinaryContent findByUserId(UUID userId);

    void delete(UUID id);

    void save(BinaryContent profileImage);
}
