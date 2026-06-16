package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusService {

    ReadStatusResponse create(ReadStatusCreateRequest dto);
    Optional<ReadStatusResponse> find(UUID id);
    List<ReadStatusResponse> findAllByUserId(UUID userId);
    ReadStatusResponse update(UUID id, ReadStatusUpdateRequest dto);
    void delete(UUID id);

}
