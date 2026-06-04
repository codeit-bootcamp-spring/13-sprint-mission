package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusRepository {
    UserStatus save(UUID userID);
    UserStatus findStatusByUserID(UUID userID);
    void delete(UUID userID);
}
