package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

  UserStatusDto create(UserStatusCreateRequest request);

  UserStatusDto find(UUID userStatusId);

  List<UserStatusDto> findAll();

  UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request);

  UserDto updateByUserId(UUID userId, UserStatusUpdateRequest request);

  void delete(UUID userStatusId);
}
