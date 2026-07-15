package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  // ReadStatus create(ReadStatusCreateRequest request);
  ReadStatus create(UUID userId, UUID channelId, Instant lastReadAt);

  ReadStatus find(UUID readStatusId);

  List<ReadStatus> findAllByUserId(UUID userId);

  // ReadStatus update(UUID readStatusId, ReadStatusUpdateRequest request);
  ReadStatus update(UUID readStatusId, Instant newLastReadAt);

  void delete(UUID readStatusId);
}
