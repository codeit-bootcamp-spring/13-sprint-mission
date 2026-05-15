package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel extends BaseEntity {

    private static int channelCount = 0;

    private String channelName;
    private String description;
    private ChannelType channelType;


    // 채널을 새롭게 생성할 때 기본 채널 이름+숫자가 들어가게 하기 위해 channelCount 넣음
    public Channel(UUID id, Long createdAt, Long updatedAt,
                   String channelName, String description, ChannelType channelType) {
        super(id, createdAt, updatedAt);
        channelCount++;
        this.channelName = channelName + channelCount;
        this.description = description;
        this.channelType = channelType;
    }

    public static int getChannelCount() {
        return channelCount;
    }

    public String getChannelName() {
        return channelName;
    }
    public String getDescription() {
        return description;
    }
    public ChannelType getChannelType() {
        return channelType;
    }

    // 채널 이름을 변경할 수 있게 하기 위해 생성함!
    public void updateChannelName(String channelName) {
        validateChannelName(channelName);
        this.channelName = channelName;
    }

    //  updateChannelName을 사용하기 위해 생성함!
    private void validateChannelName(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            throw new IllegalArgumentException("채널 이름은 비워둘 수 없습니다.");
        }
    }




}


