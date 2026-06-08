package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private UUID id;
    @Getter
    private Instant createdAt;
    private Instant updatedAt;

    private String content;
    private UUID channelId;
    private UUID authorId;

    public Message(String content, UUID channelId, UUID authorId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
    }

    public void update(String updatedContent) {
        this.content = updatedContent;
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Message {" + getId()
                + ", " + content
                + ", " + authorId
                + "}";
    }
}
