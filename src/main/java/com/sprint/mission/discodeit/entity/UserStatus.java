package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    public UserStatus(User user) {
        this.user = user;
        this.lastActiveAt = Instant.now();
    }

    public void updateLastSeenAt(Instant lastSeenAt) {
        if (lastSeenAt != null) {
            this.lastActiveAt = lastSeenAt;
        }
    }

    public boolean isOnline() {
        return lastActiveAt != null
                && Duration.between(lastActiveAt, Instant.now()).toMinutes() < 5;
    }
}
