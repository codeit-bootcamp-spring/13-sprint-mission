package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class BinaryContent {

    private UUID id;
    private Instant createdAt;

    private UUID messageId;

    private BinaryContent(UUID id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.messageId = null;
    }
    public void updateMessageId(UUID messageId) {
        this.messageId = messageId;
    }

}
