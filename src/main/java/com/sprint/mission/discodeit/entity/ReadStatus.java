package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private UUID messageId;
    private UUID channelId;
    private Instant createdAt;
    private Instant updatedAt;

    public ReadStatus(UUID id, UUID userId, UUID channelId, UUID messageId) {
        this.id = id;
        this.userId = userId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.createdAt = Instant.now();
    }

    public void updateLastSeen() {
        this.updatedAt = Instant.now();
    }

    public boolean isRead(Message message) {
        return updatedAt != null;
    }

}
