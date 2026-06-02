package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends EntityRoot implements Serializable {

    //필드
    private String message;
    private final User user;
    private final Channel channel;
    private final List<UUID> attachmentIds;

    //ctor
    public Message(String message, User user, Channel channel, List<UUID> attachmentIds) {
        super();

        this.message = message;
        this.user = user;
        this.channel = channel;
        this.attachmentIds = attachmentIds;
    }

    //update Method
    public void updateMessage(String newMessage) {
        if (newMessage == null || newMessage.isBlank())
            return;
        this.message = newMessage;
        updateUpdatedAt();
    }
    public void addAttachmentId(UUID attachmentId) {
        if(attachmentIds.contains(attachmentId) || attachmentId == null)
            return;

        attachmentIds.add(attachmentId);
        updateUpdatedAt();
    }
    public void removeAttachmentId(UUID attachmentId) {
        if (!attachmentIds.contains(attachmentId) || attachmentId == null)
            return;

        attachmentIds.remove(attachmentId);
        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "User: " + user.getName() + ", Channel: " + channel.getName() + "\n[Message: " + message + "]";
    }
}
