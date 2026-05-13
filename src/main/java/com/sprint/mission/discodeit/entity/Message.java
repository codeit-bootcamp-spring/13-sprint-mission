package com.sprint.mission.discodeit.entity;

import java.util.List;
import java.util.UUID;

public class Message {

    //공통 필드
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    //필드
    private String message;
    private User user;
    private Channel channel;

    //ctor
    public Message(String message, User user, Channel channel) {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();

        this.message = message;
        this.user = user;
        this.channel = channel;
    }

    //getter
    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    //method
    public void updateMessage(Message message) {
        if (message.message == null || message.message.isBlank())
            return;
        this.message = message.message;
    }

}
