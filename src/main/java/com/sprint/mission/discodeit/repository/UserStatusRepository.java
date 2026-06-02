package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusRepository {
    public UserStatus create();
    public UserStatus getStatusByUserID(UUID id);
    public void delete(UUID id);
}
