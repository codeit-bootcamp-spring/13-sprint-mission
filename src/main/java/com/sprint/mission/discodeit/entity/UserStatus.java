package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus {

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    //
    private UUID userId;
    private Instant lastActiveAt;
    private boolean isOnline;

    @Builder
    public UserStatus(
            UUID id, Instant createdAt, Instant updatedAt,
            UUID userId, Instant lastActiveAt, boolean isOnline) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.lastActiveAt = lastActiveAt;
        this.isOnline = isOnline;
    }



}
