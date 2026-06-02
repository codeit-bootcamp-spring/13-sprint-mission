package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus {

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private final UUID userId;
    private Instant lastAccessedAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.lastAccessedAt = Instant.now();
        this.userId = userId;
    }

    public boolean isOnline(){
        Instant nowTime = Instant.now();
        Duration between = Duration.between(lastAccessedAt, nowTime);
        return between.compareTo(Duration.ofMinutes(5)) <= 0;
    }
}
