package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

  ReadStatusDto create(ReadStatusCreateRequest request);

  ReadStatusDto find(UUID readStatusId);

  List<ReadStatusDto> findAllByUserId(UUID userId);

  ReadStatusDto update(UUID readStatusId,
      ReadStatusUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다

  void delete(UUID readStatusId);
}
