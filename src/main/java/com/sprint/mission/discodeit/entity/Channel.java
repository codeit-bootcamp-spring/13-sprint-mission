package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Channel {
    private final String id;
    private final Long createdAt;
    private Long updatedAt;

    public Channel() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }


    public void update() {
        this.updatedAt = System.currentTimeMillis();
    }
}