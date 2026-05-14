package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String content;
    private UUID channelId;
    private UUID authorId;

    public Message(String content, Channel channel, User author) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.content = content;
        this.channelId = channel.getId();
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


    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }

    public String toString() {
        return "Message{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", channelId=" + channelId +
                ", authorId=" + authorId +
                '}';
    }
}
