package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {

    private final UUID id;
    private String name;
    private final long createdAt;
    private long updatedAt;
    private ChannelType channelType;

    public Channel(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.channelType = channelType.PUBLIC;
    }
    public Channel(String name, ChannelType channelType) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.channelType = channelType;
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

    public ChannelType getChannelType() {
        return channelType;
    }

    public void updateName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateIsChannelType(ChannelType ChannelType) {
        this.channelType = ChannelType;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", channelType=" + channelType.getDisplayName() +
                '}';
    }
}
