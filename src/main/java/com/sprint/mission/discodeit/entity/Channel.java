package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID channelId;
    private String name;
    private final Instant createdAt;
    private Instant updatedAt;
    private ChannelType channelType;
    private String description;

    //공개 채널
    public Channel(String name, String description) {
        this.channelId = UUID.randomUUID();
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.channelType = channelType.PUBLIC;
    }

    //비공개 채널
    public Channel(String name, String description, ChannelType channelType) {
        this.channelId = UUID.randomUUID();
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.channelType = channelType;
    }

    public void updateChannel(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void updateChannelDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void updateIsChannelType(ChannelType ChannelType) {
        this.channelType = ChannelType;
        this.updatedAt = Instant.now();
    }
}
