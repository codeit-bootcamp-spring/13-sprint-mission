package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    UserStatus createUserStatus(UserStatusCreateRequest request);
    UserStatus findUserStatusById(UUID userStatusId);
    List<UserStatus> findAllUserStatus();
    UserStatusUpdateResponse updateUserStatus(UUID userStatusId, UserStatusUpdateRequest request);
    UserStatusUpdateResponse updateUserStatusByUserId(UUID userId, UserStatusUpdateRequest request);
    void deleteUserStatus(UUID userStatusId);

}
