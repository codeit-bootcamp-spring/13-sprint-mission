package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private ChannelType type;
    private String channelName;
    private String description;

    public Channel(ChannelType type, String channelName, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;

        this.type = type;
        this.channelName = channelName;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public ChannelType getType() {
        return type;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getDescription() {
        return description;
    }

    public void update(ChannelType type, String channelName, String description) {
        this.type = type;
        this.channelName = channelName;
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }
}
