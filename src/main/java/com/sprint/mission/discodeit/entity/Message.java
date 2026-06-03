package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;
    private String content;
    private UUID userId;
    private UUID channelId;

    public Message(String content, UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now().toEpochMilli();
        this.content = content; this.userId = userId;
        this.channelId = channelId;
    }
    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getContent() {
        return content;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = Instant.now().toEpochMilli();
    }
}