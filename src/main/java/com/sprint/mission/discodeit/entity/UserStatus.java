package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends EntityRoot {

    // 필드
    private final UUID userId;
    private Instant lastAccessTime;
    private boolean userOnline;

    //ctor
    public UserStatus(UUID userId) {
        this.userId = userId;
    }

    //getter
    // 유저 온라인 상태인지 체크 (5분 59초까지 온라인 상태인걸로)
    public boolean isUserOnline() {
        return Duration.between(lastAccessTime, Instant.now()).toMinutes() <= 5;
    }

    //updateMethod
    public void updateLastAccessTime() {
        this.lastAccessTime = Instant.now();
    }

}
