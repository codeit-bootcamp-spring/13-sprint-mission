package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

public class User implements Serializable {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String name;

    public User(String name) {
        this.id = UUID.randomUUID();
        this.createdAt = currentTimeMillis();
        this.updatedAt = createdAt;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void update(String name) {
        this.name = name;
        this.updatedAt = currentTimeMillis();
    }

}
