package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {

    private final UUID id;
    private final Instant createdAt;

    //파일 정보
    private final UUID userId; // 프로필이미지 등록용 id
    private final String fileName;
    private final long fileSize;
    private final String contentType;
    private final byte[] bytes;


    public BinaryContent(UUID userId, String fileName, long fileSize, String contentType, byte[] bytes) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.createdAt = Instant.now();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return bytes.clone();
    }
}
