package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_statuses")
@Getter
public class UserStatus extends BaseUpdatableEntity {

    private UUID userId;
    private Instant lastActiveAt;

    public UserStatus() {
    }

    public UserStatus(UUID userId, Instant lastActiveAt) {
        this.userId = userId;
        this.lastActiveAt = lastActiveAt;
    }

    public void updateLastActiveAt(Instant lastActiveAt){
        this.lastActiveAt = lastActiveAt;
    }

    public boolean isOnline() {
        return  lastActiveAt.isAfter(Instant.now().minusSeconds(300));
    }




}
