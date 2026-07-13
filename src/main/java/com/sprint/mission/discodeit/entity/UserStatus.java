package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant lastActiveAt;

    private UserStatus(User user, Instant lastActiveAt) {
        this.user = user;
        this.lastActiveAt = lastActiveAt;
    }

    public static UserStatus create(User user, Instant lastActiveAt) {
        return new UserStatus(user, lastActiveAt);
    }

    public void updateLastActiveAt(Instant newLastActiveAt) {
        if (newLastActiveAt != null) {
            this.lastActiveAt = newLastActiveAt;
        }
    }

    public boolean isOnline() {
        return lastActiveAt != null
                && Duration.between(lastActiveAt, Instant.now()).toMinutes() <= 5;
    }
}