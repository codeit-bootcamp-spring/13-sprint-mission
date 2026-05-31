package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message extends CoreEntity{

    // 필드
    private String content;
    private UUID channelId;
    private UUID authorId;

    // 생성자
    public Message(String content, UUID channelId, UUID authorId) {
        super();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
    }

    // update
    public void updateContent(String newContent) {
        this.content = newContent;
        this.update();
    }

    public void updateChannelId(UUID channelId) {
        this.channelId = channelId;
        this.update();
    }

    public void updateAuthorId(UUID authorId) {
        this.authorId = authorId;
        this.update();
    }

    // getter
    public String getContent() {
        return content;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

}