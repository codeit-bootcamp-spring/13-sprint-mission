package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;
@Getter
public class BinaryContent implements Serializable {

    private final UUID id;
    private final UUID userId;
    private final UUID messageId;
    private final String contentType;
    private final byte[] data;
    private final String fileName;
    private final Instant createdAt;

    public BinaryContent(UUID userId, UUID messageId,
                         String contentType, byte[] data,
                         String fileName) {

        this.id = UUID.randomUUID();
        validateOwner(userId, messageId);
        validateFileName(fileName);
        validateContentType(contentType);

        this.userId = userId;
        this.messageId = messageId;
        this.contentType = contentType;
        this.data = copyData(data);
        this.fileName = fileName;
        this.createdAt = Instant.now();
    }

    private void validateOwner(UUID userId, UUID messageId) {
        if (userId == null && messageId == null) {
            throw new IllegalArgumentException("userId 또는 messageId 중 하나는 필요합니다.");
        }
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름을 적어주세요.");
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("파일 타입을 적어주세요.");
        }
    }

    private void valiDate(byte[] data) {
        if (data == null || data.length == 0 ) {
            throw new IllegalArgumentException("파일의 내용이 없습니다. 확인해주세요");
        }
    }

    private byte[] copyData(byte[] data) {
        valiDate(data);
        return Arrays.copyOf(data, data.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }




}
