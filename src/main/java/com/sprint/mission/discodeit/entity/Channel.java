package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class Channel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private final ChannelType type;
    private String channelName;
    private String description;

    public Channel(ChannelType type, String channelName, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;

        this.type = type;
        this.channelName = channelName;
        this.description = description;
    }

    public void update(String channelName, String description) {
        this.updatedAt = Instant.now();

        this.channelName = channelName;
        this.description = description;
    }
}
