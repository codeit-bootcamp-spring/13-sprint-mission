package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusService {

    UserStatusResponse create(UserStatusCreateRequest dto);
    Optional<UserStatusResponse> find(UUID id);
    List<UserStatusResponse> findAll();
    UserStatusResponse update(UUID id, UserStatusUpdateRequest dto);
    UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest dto);
    void delete(UUID id);

}
