package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class UserStatus extends BaseEntity {
    private final UUID userID;
    private Instant lastLogin;

    public boolean online(){
        return 5 > Duration.between(lastLogin, Instant.now()).abs().toMillis();
    }

}
