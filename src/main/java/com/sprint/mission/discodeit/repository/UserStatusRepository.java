package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface UserStatusRepository {
    void save(UserStatus usr);
    List<UserStatus> find(Predicate<UserStatus> fn);
    List<UserStatus> findAll();
    Optional<UserStatus> findByID(UUID id);
    Optional<UserStatus> findByUserID(UUID userID);
    void delete(UUID userID);
}
