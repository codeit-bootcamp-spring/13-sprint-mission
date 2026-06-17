package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus extends MutableEntity {

    private final UUID userId;
    private final UUID channelId;
    private Instant lastReadAt;

    public ReadStatus(UUID userId, UUID channelId, Instant readAt) {
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = readAt;
    }

    public void updateReadAt(Instant readAt) {
        this.lastReadAt = readAt;
        updateTime();
    }
}
