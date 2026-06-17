package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {
    UserStatus save(UserStatus status);
    Optional<UserStatus> findById(UUID id); // 단일 조회
    Optional<UserStatus> findByUserId(UUID userId);
    boolean existById(UUID id);
    void deleteById(UUID id);
    List<UserStatus> findAll();
}
