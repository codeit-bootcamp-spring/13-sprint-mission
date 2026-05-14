package com.sprint.mission.discodeit.entity;


public enum ChannelType {
    PUBLIC("공개"),
    PRIVATE("비공개");

    private final String displayName;

    ChannelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}



