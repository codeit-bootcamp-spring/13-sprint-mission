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

//  getter
    public String getChannelName() {
        return channelName;
    }
    public String getDescription() {
        return description;
    }
    public ChannelType getChannelType() {
        return channelType;
    }
    public User getOwnerUser() {
        return ownerUser;
    }
    public List<User> getMembers() {
        return members;
    }


    // 변경할 수 있게 하기 위해 생성함!
    public void updateChannelName(String channelName) {
        validateChannelName(channelName);
        this.channelName = channelName;
    }

    public void updateDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void updateChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }


    //  예외 처리!
    private void validateChannelName(String channelName) {
        if (channelName == null || channelName.isEmpty()) {
            throw new IllegalArgumentException("채널 이름은 비워둘 수 없습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("채널 정보는 비워둘 수 없습니다.");
        }
    }


    @Override
    public String toString() {

        String memberNames = "";

        for (User member : members) {
            memberNames += member.getUsername() + " ";
        }
        return  "| Channel Name : " + channelName + '\n' +
                "| description : " + description + '\n' +
                "| Channel Type : " + channelType + '\n' +
                "| OwnerUser : " + ownerUser.getUsername() + '\n' +
                "| Members : " + memberNames + '\n';
    }

    // 채널에 유저 추가
    public void addMember(User user) {
        members.add(user);
    }

}


