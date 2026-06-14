package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@Getter
@ToString
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public void update(ChannelType type, String channelName, String description) {
        this.type = type;
        this.channelName = channelName;
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }
}
