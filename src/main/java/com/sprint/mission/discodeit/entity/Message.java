package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    private String content;
    private UUID channelId;
    private UUID authorId;

    public Message(String content, Channel ch, User author) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.content = content;
        this.channelId = ch.getId();
        this.authorId = author.getId();
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getContent() {
        return content;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void update(String updatedContent) {
        this.content = updatedContent;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Message {" + getId()
                + ", " + content
                + ", " + authorId
                + "}";
    }
}
