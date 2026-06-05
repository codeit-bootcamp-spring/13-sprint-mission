package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {

    void createBinaryContent(BinaryContent binaryContent);
    Optional<BinaryContent> findBinaryContentByContentPath(String contentPath);
    Optional<BinaryContent> findBinaryContentById(UUID binaryContentId);
    List<BinaryContent> findAllBinaryContentByIdIn(List<UUID> binaryContentIds);
    void deleteBinaryContent(UUID id);

}
