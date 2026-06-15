package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
//이미지,파일 등 바이너리 데이터를 표현하는 도메인 모델(사용자의 프로필 이미지, 메시지에 첨부된 파일을 저장하기 위해 활용됨)
public class BinaryContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String fileName;
    private String contentType;
    private byte[] data;
    private Instant updatedAt;
    private Instant createdAt;

    public BinaryContent(String fileName, String contentType, byte[] data) {
        this.id = UUID.randomUUID();
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.createdAt = Instant.now();
    }

}
