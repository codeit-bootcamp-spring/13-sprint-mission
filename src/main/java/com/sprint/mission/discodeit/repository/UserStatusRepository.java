package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusRepository {
    public UserStatus save(UUID userID);
    public UserStatus findStatusByUserID(UUID userID);
    public void delete(UUID userID);
}
