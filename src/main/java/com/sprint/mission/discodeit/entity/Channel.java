package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {
    private final String id;
    private final Long createdAt;
    private Long updatedAt;

    public Channel() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }

    public void update() {
        this.updatedAt = System.currentTimeMillis();
    }
}