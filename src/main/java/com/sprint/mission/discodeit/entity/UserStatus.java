package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends MutableEntity{

    private final UUID userId;
    private Instant lastSeenAt;

    public UserStatus(UUID userId, Instant lastSeenAt) {
        this.userId = userId;
        this.lastSeenAt = lastSeenAt;
    }

    public void updateLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
        updateTime();
    }

    public boolean isOnline() {
        return lastSeenAt != null &&
                Duration.between(lastSeenAt, Instant.now()).toMinutes() < 5;
    }
}
