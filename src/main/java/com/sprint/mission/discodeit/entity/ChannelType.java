package com.sprint.mission.discodeit.entity;

public enum ChannelType {
    PUBLIC("public"),
    PRIVATE("private");

    private final String type;
    ChannelType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}
