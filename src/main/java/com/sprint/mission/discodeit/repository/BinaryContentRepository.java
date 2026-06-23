package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {

    void save(BinaryContent profileImage);

    Optional<BinaryContent> findById(UUID id);

    List<BinaryContent> findAll();

    List<BinaryContent> findAllByIdIn(List<UUID> ids);

    List<BinaryContent> findAllByMessageId(UUID messageId);

    void delete(UUID id);

    List<BinaryContent> findByUserId(UUID userId);
}
