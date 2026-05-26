package com.sprint.mission.discodeit.entity;

import java.util.*;

public class BaseEntity {

    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = null;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
