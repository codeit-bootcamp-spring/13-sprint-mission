package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    UserStatus create(UUID userId, Instant lastActiveAt);
    UserStatus find(UUID userStatusId);
    List<UserStatus> findAll();
    UserStatus update(UUID userStatusId, Instant newLastActiveAt);
    UserStatus updateByUserId(UUID userId, Instant newLastActiveAt);
    void delete(UUID userStatusId);
}
