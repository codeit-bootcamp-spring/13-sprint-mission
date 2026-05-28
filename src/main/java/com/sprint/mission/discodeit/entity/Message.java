package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String content;
    private UUID userId;
    private UUID channelId;

    public Message(UUID userId, UUID channelId, String content) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;

        this.userId = userId;
        this.channelId = channelId;
        this.content = content;
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                '}';
    }
}
