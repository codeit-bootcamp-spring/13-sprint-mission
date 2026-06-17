package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus {

    private UUID id;
    private UUID userId;

    private Instant lastActiveAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;

        this.lastActiveAt = Instant.now();

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isOnline() {
        return lastActiveAt.isAfter(
                Instant.now().minusSeconds(300)
        );
    }
}