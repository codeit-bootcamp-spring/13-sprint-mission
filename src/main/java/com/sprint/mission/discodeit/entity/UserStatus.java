package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class UserStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID userId;
    private final Instant createdAt;
    private Instant updatedAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void updateActiveTime(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOnline() {
        return updatedAt.plusSeconds(300).isAfter(Instant.now());
    }

}
