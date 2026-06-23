package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.BinaryContentInput;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent create(BinaryContentInput bci);
    BinaryContent findByID(UUID id);
    List<BinaryContent> findAllByIdIn(List<UUID> ids);
    void delete(UUID id);
}
