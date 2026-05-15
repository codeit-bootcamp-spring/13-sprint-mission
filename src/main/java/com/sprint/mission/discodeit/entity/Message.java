package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message extends BaseEntity{

private String content;
private UUID authorId;
private UUID channelId;

    public Message(UUID id, Long createdAt, Long updatedAt) {
        super(id, createdAt, updatedAt);
    }

    public String getContent() {
        return content;
    }
    public UUID getAuthorId() {
        return authorId;
    }
    public UUID getChannelId() {
        return channelId;
    }

    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }

    private void validateContent(String content) {
            if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메세지 내용을 입력하세요.");
        }
    }





}
