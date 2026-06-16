package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {

    BinaryContent save(BinaryContent profileImage);

    BinaryContent findById(UUID Id);

    List<BinaryContent> findAll();

    List<BinaryContent> findAllByIdIn(List<UUID> ids);

    List<BinaryContent> findAllByMessageId(UUID messageId);

    void delete(UUID id);

}
