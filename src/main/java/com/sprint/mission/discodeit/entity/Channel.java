package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    private String name;
    private ChannelType type;
    private String description;

    public enum ChannelType {
        PUBLIC, PRIVATE
    }

    public Channel(String name, ChannelType type, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
    }

    public String getDescription() {
        return description;
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

        this.updatedAt = System.currentTimeMillis();
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
