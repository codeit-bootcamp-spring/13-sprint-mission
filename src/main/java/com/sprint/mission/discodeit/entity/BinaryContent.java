package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {

    private UUID id;

    private UUID userId;
    private UUID messageId;

    private String fileName;
    private String contentType;

    private byte[] data;

    private Instant createdAt;

    public BinaryContent(
            UUID userId,
            UUID messageId,
            String fileName,
            String contentType,
            byte[] data
    ) {
        this.id = UUID.randomUUID();

        this.userId = userId;
        this.messageId = messageId;

        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;

        this.createdAt = Instant.now();
    }
}