package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    private String name;
    private String description;

    public Channel(String name, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now().toEpochMilli();

        this.name = name;
        this.description = description;
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now().toEpochMilli();
    }
}