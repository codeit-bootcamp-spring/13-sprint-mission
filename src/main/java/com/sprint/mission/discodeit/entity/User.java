package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class User {
    private final String id;
    private final Long createdAt;
    private Long updatedAt;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    // Getter
    public String getId() { return id; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }

    // 수정
    public void update() {
        this.updatedAt = System.currentTimeMillis();
    }
}