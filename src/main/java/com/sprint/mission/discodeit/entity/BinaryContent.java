package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {

    //필드
    private final UUID id;
    private final Instant createdAt;

    //개선 필드
    private final String fileName;
    private final Long size;
    private final String contentType;
    private final byte[] bytes;

    //ctor
    public BinaryContent(String fileName, Long size, String contentType, byte[] bytes) {
        id = UUID.randomUUID();
        createdAt = Instant.now();

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.bytes = bytes;
    }
}
