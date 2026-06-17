package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final Instant createdAt;

    private Instant updatedAt;

    private String content;

    private final UUID channelId;

    private final UUID authorId;

    private final List<UUID> attachmentIds;

    public Message(String content, UUID channelId, UUID authorId) {

        this(content, channelId, authorId, null);
    }

    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = attachmentIds == null ? new ArrayList<>()
                : new ArrayList<>(attachmentIds);
    }

    public void update(String newContent) {
        boolean flag = false;

        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
            flag = true;
        }

        if (flag) {
            this.updatedAt = Instant.now();
        }
    }
}
