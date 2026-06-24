package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private UUID userId;
    private UUID messageId;
    private Instant createdAt;

    private String filename;
    private String contentType;
    private int fileSize;
    private byte[] bytes;

    public BinaryContent(UUID userId, UUID messageId, String filename, String contentType, byte[] bytes) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.messageId = messageId;
        this.createdAt = Instant.now();

        this.filename = filename;
        this.contentType = contentType;
        this.bytes = bytes;
        this.fileSize = bytes.length;
    }

}