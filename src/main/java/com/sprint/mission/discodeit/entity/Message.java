package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID messageId;
    private final long createdAt;
    private long updatedAt;
    private String content;
    private final UUID channelId;
    private final UUID authorId;

    public Message(String content, UUID channelId, UUID authorId) {
        this.messageId = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
    }

    public UUID getId() {
        return messageId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
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

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdAt = sdf.format(new Date(this.createdAt));
        String updatedAt = sdf.format(new Date(this.updatedAt));
        return "Message{" +
                "id=" + messageId +
                ", 생성 시간 = " + createdAt +
                ", 업데이트 시간 = " + updatedAt +
                ", 메시지 = " + content + '\'' +
                ", 채널 id= " + channelId +
                ", 작성자 id= " + authorId +
                '}';
    }
}
