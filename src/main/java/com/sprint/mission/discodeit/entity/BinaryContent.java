package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private final UUID id;
    private final Instant createdAt;
    // 수정 불가능한 도메인 모델로 간주하기 때문에 updatedAt 필드 정의하지 않음
    private String fileName, contentType;
    private int fileSize;

    public BinaryContent(String fileName, String contentType, int fileSize) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }
}
