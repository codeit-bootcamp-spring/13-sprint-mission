package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.input.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatus create(ReadStatusCreateRequest crsi);
    List<ReadStatus> findAllByUserID(UUID userID);
    ReadStatus update(UUID id, ReadStatusUpdateRequest rsur);
    void delete(UUID id);
}
