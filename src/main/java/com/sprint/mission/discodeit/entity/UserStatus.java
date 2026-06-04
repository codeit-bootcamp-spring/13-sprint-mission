package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserStatus extends BaseEntity {
    private final UUID userID;
    private Instant lastLogin;

    public boolean online(){
        return 5 > Duration.between(lastLogin, Instant.now()).abs().toMillis();
    }

}
