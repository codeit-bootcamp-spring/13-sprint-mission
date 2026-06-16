package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentResponse create(BinaryContentRequest dto);
    Optional<BinaryContentResponse> find(UUID id);
    List<BinaryContentResponse> findAllByIdIn(List<UUID> ids);
    void delete(UUID id);
}
