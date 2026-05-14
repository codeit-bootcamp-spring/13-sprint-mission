package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String channelName;
    private String description;
    //private ChannelType typ;


    public Channel(String chName,String description) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.channelName = chName;
        this.description = description;
        //this.type = ChannelType;(나중에 클레스 만들면 매개변수에 추가 해야함.)
    }


    public UUID getId() {return id;}
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}
    public String getChannelName() {return channelName;}
    public String getDescription() {return description;}
    //public ChannelType getType() {return type};


    public void updateChName(String newChannelName){
        this.channelName = newChannelName;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateDescription(String newDescription){
        this.description = newDescription;
        this.updatedAt = System.currentTimeMillis();
    }
    //public void updateType(ChannelType newType)
}

