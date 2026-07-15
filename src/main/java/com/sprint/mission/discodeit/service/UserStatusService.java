package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;

import java.util.Collection;
import java.util.UUID;

public interface UserStatusService {
    UserStatusDto create(UserStatusCreateRequest request);
    UserStatusDto findById(UUID id);
    Collection<UserStatusDto> findAll();
    UserStatusDto update(UserStatusUpdateRequest request);
    UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request);
    UserStatusDto findByUserId(UUID userId);
    void delete(UUID id);

}
