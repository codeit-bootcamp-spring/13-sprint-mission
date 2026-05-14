package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {
    // delete 추가 예정 - 지금은 복잡해서 나중에.
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
    // delete 추가 필요한데, 나중에 하고 업데이트 뭐뭐 해야하는지 헷갈리는 상태.
    // id createdAt updatedAt content channel author

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateChannel(UUID channelId) {
        this.channelId = channelId;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateAuthor(UUID authorId) {
        this.authorId = authorId;
        this.updatedAt = System.currentTimeMillis();
    }



}
