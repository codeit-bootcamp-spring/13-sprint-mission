package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.util.UUID;

// memo - class ID 가 달라졌었음. 필드 받아가는 로직을 Getter로 바꾼것 뿐인데 왜?


@Getter
public class Message extends BaseEntity {
    private final UUID userID;
    private final UUID channelID;

    // make with generic?
    // now first make this at simple string
    private String data;


    public Message(UUID userID, UUID channelID, String data) {
        super();
        this.userID = userID;
        this.channelID = channelID;
        this.data = data;
    }

    // temp ToString
    @Override
    public String toString(){
        String res = " ===== Message ====== \n"
                + "id : "  + this.getId() + "\n"
                + "createdAt : " + this.getCreatedAt() + "\n"
                + "updatedAt :" + this.getUpdatedAt() + "\n"
                + "userId : " + this.userID.toString() + "\n"
                + "channelId :" + this.channelID.toString() + "\n"
                + "data : " + this.data + "\n";
        return res;
    }

    public void setMessages(String data) {
        this.data = data;
    }


}
