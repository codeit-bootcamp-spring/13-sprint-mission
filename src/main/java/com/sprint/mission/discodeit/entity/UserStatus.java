package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

@Getter
public class UserStatus extends BaseUpdatableEntity {

    private User user;
    private Instant lastActiveAt;

    public UserStatus(User user) {
        super();
        this.user = user;
        this.lastActiveAt = Instant.now();
    }

    public boolean isOnline(){
        Instant nowTime = Instant.now();
        Duration between = Duration.between(lastActiveAt, nowTime);
        return between.compareTo(Duration.ofMinutes(5)) <= 0;
    }

    public void updateLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

}
