package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
@Getter
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private ChannelType type;
    private String channelName;
    private String description;

    public Channel(ChannelType type, String chName, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now().getEpochSecond();
        this.updatedAt = this.createdAt;
        this.type = type;
        this.channelName = chName;
        this.description = description;
    }

    public void update(String chName, String chDescription) {
        boolean anyValueUpdated = false;

        if (chName != null && !chName.equals(this.channelName)) {
            this.channelName = chName;
            anyValueUpdated = true;
        }
        if (chDescription != null && !chDescription.equals(this.description)) {
            this.description = chDescription;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now().getEpochSecond();
        }
    }
}