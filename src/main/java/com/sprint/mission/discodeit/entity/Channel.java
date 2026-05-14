package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {

    private final UUID id;
    private String name;
    private final long createdAt;
    private long updatedAt;
    private boolean channelType;

    public Channel(String name, boolean ChannelType) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.channelType = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isChannelType() {
        return channelType;
    }

    public void updateName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateIsChannelType(boolean ChannelType) {
        this.channelType = ChannelType;
        this.updatedAt = System.currentTimeMillis();
    }
}
