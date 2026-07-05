package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatusDto createUserStatus(UserStatusCreateRequest request);
    UserStatusDto findUserStatusById(UUID userStatusId);
    List<UserStatusDto> findAllUserStatus();
    UserStatusDto updateUserStatusByUserId(UUID userId, UserStatusUpdateRequest request);
    void deleteUserStatus(UUID userStatusId);

}
