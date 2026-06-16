package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    //
    private User user;
    private Instant lastActiveAt;

    @Builder
    public UserStatus(
            UUID id, Instant createdAt, Instant updatedAt,
            User user, Instant lastActiveAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.lastActiveAt = lastActiveAt;
    }

    public boolean isOnline() {
        if (this.lastActiveAt == null) return false;

        Instant now = Instant.now();
        Instant fiveMinuteAgo = now.minus(java.time.Duration.ofMinutes(5));

        return this.lastActiveAt.isAfter(fiveMinuteAgo);
    }

    public void updateActiveTime() {
        this.lastActiveAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isUser(UUID userId) {
        return user != null && user.getId().equals(userId);
    }
}
