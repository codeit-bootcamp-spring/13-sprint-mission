package com.sprint.mission.discodeit.entity;


public class Channel extends BaseEntity {

    private ChannelType type; // 채널 공개여부
    private String description; // 채널 소개
    private String name; // 채널 이름

    public Channel(String name, String description, ChannelType type) {
        super();
        this.name = validateName(name); // 채널 이름
        this.description = validateDescription(description); // 채널 설명
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("채널 소개는 필수입니다.");
        }

        return description;
    }

    public String getName() {
        return name;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채널 이름은 필수입니다.");
        }

        return name;
    }


    public ChannelType getType() {
        return type;
    }

    public void setType(ChannelType type) {
        this.type = validateType(type);
    }

    private ChannelType validateType(ChannelType type) {
        if (type == null) {
            return ChannelType.PUBLIC;
        }

        return type;
    }

    @Override
    public String toString() {
        return "Channel: " +
                "공개여부 = " + type.getLabel() +
                ", 채널 이름= '" + name + '\'' +
                ", 채널 소개= '" + description + '\'';
    }
}
