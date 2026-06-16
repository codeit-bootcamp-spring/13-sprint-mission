package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatusResponse create(UserStatusCreateRequest request);
    UserStatusResponse find(UUID id);
    List<UserStatusResponse> findAll();
    UserStatusResponse update(UserStatusUpdateRequest request);
    UserStatusResponse updateByUserId(UUID userId, String statusName);
    void delete(UUID id);

}
