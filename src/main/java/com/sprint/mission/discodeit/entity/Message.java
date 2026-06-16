package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Getter
@ToString
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID messageId;
    private final Instant createdAt;
    private Instant updatedAt;
    private String content;
    private final UUID channelId;
    private final UUID authorId;
    private final List<UUID> attachmentIds;

    // 텍스트 + 첨부파일
    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        this.messageId = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = attachmentIds == null ? List.of() : List.copyOf(attachmentIds);
    }
    //텍스트
    public Message(String content, UUID channelId, UUID authorId) {
        this(content, channelId, authorId, List.of());
    }

    //첨부파일
    public Message(UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        this(null, channelId, authorId, attachmentIds);
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = Instant.now();
    }
}
