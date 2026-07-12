package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto create(UserStatusCreateRequest request);

  UserStatusDto find(UUID userStatusId);

  List<UserStatusDto> findAll();

  UserStatusDto update(UUID userStatusId,
      UserStatusUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다

  UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request);

  void delete(UUID userStatusId);

}
