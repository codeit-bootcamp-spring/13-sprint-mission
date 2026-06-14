package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

//사용자별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델(사용자의 온란인 상태를 확인 하기 위해 활용됨)
public class UserStatus implements Serializable {

    private final UUID id;
    private final UUID userId;
    private Instant lastConnectedAt;
    private Instant updatedAt;
    private final Instant createdAt;

    public UserStatus(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.lastConnectedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    //현재 온라인 여부 확인. 마지막 접속 시간이 5분 이내면 true
    public boolean isOnline() {
        return Duration.between(lastConnectedAt, Instant.now()).toMinutes() >= 5;
    }

    public UUID getId() {return id;}
    public UUID getUserId() {return userId;}
    public Instant getLastConnectedAt() {return lastConnectedAt;}
    public Instant getUpdatedAt() {return updatedAt;}
    public Instant getCreatedAt() {return createdAt;}

    public void updateLastConnectedAt() {
        this.lastConnectedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
