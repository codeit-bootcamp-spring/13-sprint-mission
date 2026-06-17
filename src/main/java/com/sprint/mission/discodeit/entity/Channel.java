package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;

    private String name;
    private ChannelType type;
    private String description;

    public enum ChannelType {
        PUBLIC, PRIVATE
    }

    public Channel(String name, ChannelType type, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public void update(String name, ChannelType type, String description) {
        if (name != null) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        if (description != null) {
            this.description = description;
        }

        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Channel{" + id
                + ", " + name
                + ", " + type
                + ", " + description
                + "}";
    }
}
