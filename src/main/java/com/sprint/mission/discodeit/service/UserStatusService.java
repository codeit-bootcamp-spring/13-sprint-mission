package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatusDto create(UserStatusCreateRequest request);

    UserStatusDto find(UUID id);

    List<UserStatusDto> findAll();

    UserStatusDto update(UserStatusUpdateRequest request);

    UserStatusDto updateByUserId(UserStatusUpdateByUserIdRequest request);

    void delete(UUID id);
}
