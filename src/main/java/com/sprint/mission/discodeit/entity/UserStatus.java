package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class UserStatus extends BaseUpdatableEntity {
    private final User user;
    private Instant lastActiveAt;
    // activation timeout ( 5min )
    private final Integer timeout = 5 * 60 * 1000;

    public UserStatus(
            UUID id,
            Instant ctime,
            Instant mtime,
            User user) {
        super(id,ctime, mtime);
        this.user = user;
        this.lastActiveAt = mtime;
    }

    public UserStatus(
            User user,
            Instant lastActiveAt
    ){
        super();
        this.user = user;
        this.lastActiveAt = lastActiveAt;
    }



    public boolean online(){
        return timeout > Duration.between(lastActiveAt, Instant.now()).abs().toMillis();
    }
}
