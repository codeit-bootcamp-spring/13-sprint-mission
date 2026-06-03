package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public abstract class CoreEntity {

    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    public CoreEntity() {
        // 자식 객체 생성시 이 생성자를 통해 그 자식객체에게 아래정보들을 부여함. super이용
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public void update() {
        this.updatedAt = System.currentTimeMillis();
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

}