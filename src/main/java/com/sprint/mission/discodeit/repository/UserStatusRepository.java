package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {

    boolean existsUserStatusByUserId(UUID userId);
    void createUserStatus(UserStatus userStatus);
    Optional<UserStatus> findUserStatusById(UUID readStatusId);
    Optional<UserStatus> findUserStatusByUserId(UUID userId);
    List<UserStatus> findAllUserStatus();
    void save();
    void deleteUserStatus(UUID userStatusId);

}
