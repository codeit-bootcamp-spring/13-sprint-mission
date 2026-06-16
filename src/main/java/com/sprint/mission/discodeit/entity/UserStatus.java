package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 사용자별 마지막 접속시간 표현 도메인
 */


@Getter
@Setter
@Builder
public class UserStatus extends BaseEntity {
    private final UUID userID;
    private Instant lastLogin;
    private final Integer timeout = 5 * 60 * 1000;

    public boolean online(){
        System.out.println(lastLogin);
        System.out.println(Instant.now());
        System.out.println(Duration.between(lastLogin, Instant.now()).abs().toMillis());
        return timeout > Duration.between(lastLogin, Instant.now()).abs().toMillis();
    }

}
