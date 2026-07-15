package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;

import java.util.Collection;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusDto create(ReadStatusCreateRequest request);
    ReadStatusDto findById(UUID id);
    Collection<ReadStatusDto> findAllByUserId(UUID userId);
    ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request);
    void delete(UUID id);
}
