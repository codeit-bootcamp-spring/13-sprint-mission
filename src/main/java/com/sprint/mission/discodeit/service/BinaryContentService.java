package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Collection;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContentResponse create(BinaryContentCreateRequest request);
    BinaryContentResponse findById(UUID id);
    Collection<BinaryContentResponse> findAllByIdIn(Collection<UUID> ids);
    void delete(UUID id);
    BinaryContent findEntityById(UUID id);
}
