package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
public class UserStatus {

    private UUID id;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.lastAt = this.createdAt;
    }

    public void setLastAt() {
        this.lastAt = Instant.now();
        this.updatedAt = this.lastAt;
    }

    public boolean isOnline() {
        if (this.lastAt == null) {
            return false;
        }
        return this.lastAt.isAfter(Instant.now().minusSeconds(300));
    }

}
