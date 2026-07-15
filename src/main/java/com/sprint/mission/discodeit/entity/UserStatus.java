package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "online", nullable = false)
    private boolean online;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    public UserStatus(UUID userId) {
        validate(userId);

        this.userId = userId;
        this.online = true;
        this.lastActiveAt = Instant.now();
    }

    public void updateOnline() {
        this.online = true;
        this.lastActiveAt = Instant.now();
        markUpdated();
    }

    public void updateOffline() {
        this.online = false;
        this.lastActiveAt = Instant.now();
        markUpdated();
    }

    public void updateLastActiveAt() {
        updateLastActiveAt(Instant.now());
    }

    public void updateLastActiveAt(Instant lastActiveAt) {
        if (lastActiveAt == null) {
            throw new IllegalArgumentException("마지막 활동 시간은 비어 있을 수 없습니다.");
        }

        this.online = true;
        this.lastActiveAt = lastActiveAt;
        markUpdated();
    }

    public boolean isOnline() {
        return online;
    }

    private void validate(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 비어 있을 수 없습니다.");
        }
    }
}