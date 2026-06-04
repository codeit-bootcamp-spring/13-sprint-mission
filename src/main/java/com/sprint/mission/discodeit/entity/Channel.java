package com.sprint.mission.discodeit.entity;


import java.io.*;

public class Channel extends BaseEntity implements Serializable {

    private ChannelType type; // 채널 공개여부
    private String description; // 채널 소개
    private String name; // 채널 이름

    public Channel(String name, String description, ChannelType type) {
        super();
        validateName(name); // 채널 이름
        validateDescription(description); // 채널 설명
        validateType(type);
    }

    public String getDescription() {
        return description;
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("채널 소개는 필수입니다.");
        }
        this.description = description;

    }
    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
        setUpdatedAt();
    }

    public String getName() {
        return name;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채널 이름은 필수입니다.");
        }
        this.name = name;
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
        setUpdatedAt();
    }

    public ChannelType getType() {
        return type;
    }

    private void validateType(ChannelType type) {
        if (type == null) {
            throw new RuntimeException("채널 타입을 설정하세요.");
        }
        this.type = type;
    }

    public void updateType(ChannelType type) {
        validateType(type);
        this.type = type;
        setUpdatedAt();
    }

    public void update(String name, String description, ChannelType type) {
        updateName(name);
        updateDescription(description);
        updateType(type);
    }

    @Override
    public String toString() {
        return "Channel: " +
                "공개여부 = " + type.getLabel() +
                ", 채널 이름= '" + name + '\'' +
                ", 채널 소개= '" + description + '\'';
    }
}
