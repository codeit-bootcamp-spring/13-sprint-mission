package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

public class Channel implements Serializable {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String room;

    public Channel(String room) {
        this.id = UUID.randomUUID();
        this.createdAt = currentTimeMillis();
        this.updatedAt = createdAt;
        this.room = room;
    }

    public String getRoom() {
        return room;
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

    public void update(String room) {
        this.room = room;
        this.updatedAt = currentTimeMillis();
    }
}
