package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {

    void createUserStatus(UserStatus userStatus);
    Optional<UserStatus> findUserStatusByUserId(UUID userId);
    void deleteUserStatus(UUID userStatusId);

}
