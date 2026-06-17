package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
//사용자별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델(사용자의 온란인 상태를 확인 하기 위해 활용됨)
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private UUID userId;
    private Instant lastConnectedAt;
    private Instant updatedAt;
    private Instant createdAt;

    public UserStatus(UUID userId, Instant lastConnectedAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.lastConnectedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public void update(Instant lastActiveAt) {
        boolean anyValueUpdated = false;
        if (lastActiveAt != null && !lastActiveAt.equals(this.lastConnectedAt)) {
            this.lastConnectedAt = lastActiveAt;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now();
        }
    }

    //현재 온라인 여부 확인. 마지막 접속 시간이 5분 이내면 true
    public Boolean isOnline() {
        Instant instantFiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(5));
        return lastConnectedAt.isAfter(instantFiveMinutesAgo);
    }

}
