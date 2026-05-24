package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Message extends EntityRoot implements Serializable {

    //필드
    private String message;
    private User user;
    private Channel channel;

    //ctor
    public Message(String message, User user, Channel channel) {
        super();

        this.message = message;
        this.user = user;
        this.channel = channel;
    }

    //getter
    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    //update Method
    public void updateMessage(String newMessage) {
        if (newMessage == null || newMessage.isBlank())
            return;
        this.message = newMessage;
        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "User: " + user.getName() + ", Channel: " + channel.getName() + "\n[Message: " + message + "]";
    }
}
