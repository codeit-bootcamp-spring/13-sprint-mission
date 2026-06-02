package com.sprint.mission.discodeit.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// memo - class ID 가 달라졌었음. 필드 받아가는 로직을 Getter로 바꾼것 뿐인데 왜?


@Getter
@AllArgsConstructor
public class Message extends BaseEntity {
    private final UUID userID;
    private final UUID channelID;
    private String data;
    private final List<UUID> attrID = new ArrayList<>();



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
    public void addAttr(UUID attrID) {
        this.attrID.add(attrID);
    }

}
