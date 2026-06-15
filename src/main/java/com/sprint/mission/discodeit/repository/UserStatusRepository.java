package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface UserStatusRepository {
    void save(UserStatus usr);
    List<UserStatus> find(Predicate<UserStatus> fn);
    List<UserStatus> findAll();
    UserStatus findByID(UUID id);
    UserStatus findByUserID(UUID userID);
    void delete(UUID userID);
}
