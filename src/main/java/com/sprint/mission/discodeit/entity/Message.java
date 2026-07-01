package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    private final UUID userId;
    private final UUID channelId;
    private String content;
    private final List<UUID> attachmentIds;

    public Message(UUID userId, UUID channelId, String content) {
        this(userId, channelId, content, new ArrayList<>());
    }

    public Message(UUID userId, UUID channelId, String content, List<UUID> attachmentIds) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;

        this.userId = userId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = attachmentIds;
    }

    public void update(String content) {
        this.updatedAt = Instant.now();
        this.content = content;
    }
}
