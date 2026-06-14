package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

//이미지,파일 등 바이너리 데이터를 표현하는 도메인 모델(사용자의 프로필 이미지, 메시지에 첨부된 파일을 저장하기 위해 활용됨)
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final String fileName;
    private final String contentType;
    private final byte[] data;
    private Instant updatedAt;
    private final Instant createdAt;

    public BinaryContent(String fileName, String contentType, byte[] data) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.createdAt = Instant.now();
    }

    public UUID getId() {return id;}
    public String getFileName() {return fileName;}
    public String getContentType() {return contentType;}
    public byte[] getData() {return data;}
    public Instant getUpdatedAt() {return updatedAt;}
    public Instant getCreatedAt() {return createdAt;}
}
