package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

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
    private String description; // 채널 설명
    private String type;

    public enum ChannelType {
        PUBLIC, PRIVATE
    }

    public Channel(String name, String description, String type) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public void update(String name, String description) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        this.updatedAt = Instant.now();
    }


    public void updateName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }
    public void updateType(String type) {
        this.type = type;
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                '}';
    }
}
