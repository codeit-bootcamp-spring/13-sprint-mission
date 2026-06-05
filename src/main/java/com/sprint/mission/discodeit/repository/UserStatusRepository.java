package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.Collection;
import java.util.UUID;

public interface UserStatusRepository {
    void save(UserStatus userStatus);
    UserStatus findById(UUID id);
    Collection<UserStatus> findByUserId(UUID userid);
    void delete(UUID id);
}
