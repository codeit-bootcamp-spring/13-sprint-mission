package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
public class UserStatus extends BaseEntity {
    private final UUID userId;
    private Instant lastActiveAt;

    // activation timeout ( 5min )
    private final Integer timeout = 5 * 60 * 1000;


    public boolean online(){
        System.out.println(lastActiveAt);
        System.out.println(Instant.now());
        System.out.println(Duration.between(lastActiveAt, Instant.now()).abs().toMillis());
        return timeout > Duration.between(lastActiveAt, Instant.now()).abs().toMillis();
    }

}
