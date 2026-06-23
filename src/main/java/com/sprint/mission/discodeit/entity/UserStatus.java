package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends EntityRoot implements Serializable {

    // 필드
    private final UUID userId;
    private Instant lastActiveAt;
    private boolean online;

    //ctor
    public UserStatus(UUID userId) {
        super();

        this.userId = userId;
        this.lastActiveAt = Instant.EPOCH;
    }

    //getter
    // 유저 온라인 상태인지 체크 (5분 59초까지 온라인 상태인걸로)
    public boolean isUserOnline() {
        return Duration.between(lastActiveAt, Instant.now()).toMinutes() <= 5;
    }

    //updateMethod
    public void updateLastActiveAt() {
        this.lastActiveAt = Instant.now();

        updateUpdatedAt();
    }

}
