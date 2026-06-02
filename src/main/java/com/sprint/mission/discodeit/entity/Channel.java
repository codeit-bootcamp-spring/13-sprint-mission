package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String name;
    private String description; // 채널 설명
    private ChannelType type;

    public enum ChannelType {
        PUBLIC, PRIVATE
    }

    public Channel(String name, String description, ChannelType type) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.name = name;
        this.description = description;
        this.type = type;
    }


    public void updateName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateType(ChannelType type) {
        this.type = type;
        this.updatedAt = System.currentTimeMillis();
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
