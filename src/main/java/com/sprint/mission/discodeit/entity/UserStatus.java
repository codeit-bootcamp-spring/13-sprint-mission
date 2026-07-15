package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserStatus extends BaseUpdatableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_active_at")
    private Instant lastActivityAt;

    public UserStatus(User user) {
        super();
        this.user = user;
        this.lastActivityAt = null;

        if (user != null && user.getUserStatus() != this) {
            user.updateUserStatus(this);
        }
    }

    public void markOnline() {
        this.lastActivityAt = Instant.now();
    }

    public boolean isOnline() {
        if (lastActivityAt == null) return false;
        Instant now = Instant.now();
        return !lastActivityAt.isBefore(now.minus(Duration.ofMinutes(5)));
    }

    public void updateLastOnlineAt(Instant lastOnlineAt) {
        this.lastActivityAt = lastOnlineAt;
    }

}
