package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus {

    private UUID id;
    private UUID userId;
    private UUID channelId;

    private Instant lastReadAt;

    private Instant createdAt;
    private Instant updatedAt;

    public ReadStatus(UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;

        this.lastReadAt = Instant.now();

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}