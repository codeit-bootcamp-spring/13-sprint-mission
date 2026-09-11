package com.sprint.mission.discodeit.security.role;

public enum Role {
    ADMIN("admin"),
    CHANNEL_MANAGER("channelManager"),
    USER("user");

    private String value;

    Role(String value){
        this.value = value;
    }

    public String value(){ return value; }


}

