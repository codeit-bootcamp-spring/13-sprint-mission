package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

public class Message implements Serializable {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String talk;

    public Message(String talk) {
        this.id = UUID.randomUUID();
        this.createdAt = currentTimeMillis();
        this.updatedAt = createdAt;
        this.talk = talk;
    }

    public String getTalk() {
        return talk;
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
        this.talk = talk;
        this.updatedAt = currentTimeMillis();
    }

}
