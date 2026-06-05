package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
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
            UUID userId, Instant lastActiveAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.lastActiveAt = lastActiveAt;
    }

    public boolean isOnline() {
        if (this.lastActiveAt == null) return false;

        Instant now = Instant.now();
        Instant fiveMinuteAgo = now.minus(java.time.Duration.ofMinutes(5));

        return this.lastActiveAt.isAfter(fiveMinuteAgo);
    }
}
