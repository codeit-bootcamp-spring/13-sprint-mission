package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID channelId;
    private String name;
    private final long createdAt;
    private long updatedAt;
    private ChannelType channelType;
    private String description;

    //공개 채널
    public Channel(String name, String description) {
        this.channelId = UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.channelType = channelType.PUBLIC;
    }

    //비공개 채널
    public Channel(String name, String description, ChannelType channelType) {
        this.channelId = UUID.randomUUID();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.channelType = channelType;
    }

    public UUID getId() {
        return channelId;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void updateChannel(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateChannelDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateIsChannelType(ChannelType ChannelType) {
        this.channelType = ChannelType;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdAt = sdf.format(new Date(this.createdAt));
        String updatedAt = sdf.format(new Date(this.updatedAt));

        return "Channel{" +
                "id =" + channelId +
                ", 채널 이름 ='" + name + '\'' +
                ", 생성 시간 = " + createdAt +
                ", 업데이트 시간 = " + updatedAt +
                ", 채널 설명 = " + description +
                ", 공개 범위 = " + channelType.getDisplayName()+
                '}';
    }
}
