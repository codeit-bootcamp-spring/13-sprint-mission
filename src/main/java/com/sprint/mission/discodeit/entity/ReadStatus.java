package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus extends EntityRoot {

    //필드
    private final UUID userId;
    private final UUID channelId;
    private Instant lastAccessTime;

    //ctor
    public ReadStatus(UUID userId, UUID channelId) {
        this.userId = userId;
        this.channelId = channelId;
        this.lastAccessTime = Instant.now();
    }

    //updateMethod
    public void updateLastAccessTime() {
        this.lastAccessTime = Instant.now();
    }


}
