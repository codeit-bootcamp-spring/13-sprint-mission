package com.sprint.mission.discodeit.entity;

import java.time.Instant;
import java.util.UUID;

public class UserStatus {

    private UUID id;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;

    public UserStatus(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateLastSeen() {
        this.updatedAt = Instant.now();
    }

    public boolean isOnline() {
        return updatedAt.isAfter(Instant.now().minusSeconds(300));
    }

}
