package com.sprint.mission.discodeit.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Channel extends BaseEntity {


    private String channelName;
    private String description;
    private ChannelType channelType;

    private User ownerUser;
    private List<User> members = new ArrayList<>();


    public Channel(String channelName, String description,
                   ChannelType channelType,  User ownerUser) {
        super();
        this.channelName = channelName;
        this.description = description;
        this.channelType = channelType;
        this.ownerUser = ownerUser;
        this.members.add(ownerUser);
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

    @Override
    public String toString() {
        return  "| Channel Name : " + channelName + '\n' +
                "| Channel Type : " + channelType + '\n' +
                "| OwnerUser : " + ownerUser.getUsername() + '\n' +
                "| Members : " + '\n' +
                "| description : " + description + '\n';
    }



}


