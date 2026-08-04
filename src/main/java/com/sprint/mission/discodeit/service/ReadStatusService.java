package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.dto.response.ReadStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

    ReadStatusDto createReadStatus(ReadStatusCreateRequest request);
    ReadStatusDto getReadStatus(UUID readStatusId);
    List<ReadStatusDto> getReadStatusesByUserId(UUID userId);
    ReadStatusDto updateReadStatus(UUID readStatusId, ReadStatusUpdateRequest request);
    void deleteReadStatus(UUID readStatusId);
}
