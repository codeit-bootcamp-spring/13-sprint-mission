package com.sprint.mission.discodeit.entity;

import java.util.List;

public class Channel extends CoreEntity {

    // 필드
    private String channelName;
    private ChannelType channelType;
    private String channelDescription;

    // 생성자
    public Channel(String channelName, ChannelType channelType, String channelDescription) {
        super();
        this.channelName = channelName;
        this.channelType = channelType;
        this.channelDescription = channelDescription;
    }

    // update
    public void updateChannelName(String newChannelName) {
        this.channelName = newChannelName;
        this.update();
    }
    public void updateChannelType(ChannelType newChannelType) {
        this.channelType = newChannelType;
        this.update();
    }
    public void updateChannelDescription(String newChannelDescription) {
        this.channelDescription = newChannelDescription;
        this.update();
    }

    // getter
    public String getChannelName() {
        return channelName;
    }
    public ChannelType getChannelType() {
        return channelType;
    }
    public String getChannelDescription() {
        return channelDescription;
    }

}