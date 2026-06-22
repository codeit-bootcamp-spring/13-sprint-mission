package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatusResponse create(UserStatusCreateRequest request);
    UserStatusResponse find(UUID userStatusId);
    List<UserStatusResponse> findAll();
    UserStatusResponse update(UUID userStatusId, UserStatusUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다
    UserStatusResponse updateByUserId(UUID userId);
    void delete(UUID userStatusId);

}
