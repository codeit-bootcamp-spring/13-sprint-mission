package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus extends EntityRoot implements Serializable {

    //필드
    private final UUID userId;
    private final UUID channelId;
    private Instant lastReadAt;

    //ctor
    public ReadStatus(UUID userId, UUID channelId) {
        super();

        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = Instant.now();
    }

    //updateMethod
    public void updateLastReadAt() {
        this.lastReadAt = Instant.now();

        updateUpdatedAt();
    }


}
