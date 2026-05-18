package com.sprint.mission.discodeit.entity;


import java.util.UUID;


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

    public UUID getUser() {
        return userID;
    }

    public UUID getChannel() {
        return channelID;
    }

    public String getMessages() {
        return this.data;
    }

    public void setMessages(String data) {
        this.data = data;
    }


}
