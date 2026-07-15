package com.sprint.mission.discodeit.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContentResponse {

    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;

    private String fileName;

    private String contentType;

    private String type;

    private String mimeType;

    private String mediaType;

    private Long size;

    private String url;

    private String downloadUrl;

    public BinaryContentResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String fileName,
            String contentType,
            Long size
    ) {
        String safeContentType = contentType;

        if (safeContentType == null || safeContentType.isBlank()) {
            safeContentType = "application/octet-stream";
        }

        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fileName = fileName;
        this.contentType = safeContentType;
        this.type = safeContentType;
        this.mimeType = safeContentType;
        this.mediaType = safeContentType;
        this.size = size;

        /*
         * 중요:
         * /api/binaryContents/{id} 는 메타데이터 JSON 조회용
         * /api/binaryContents/{id}/download 는 실제 이미지/파일 조회용
         */
        this.url = "/api/binaryContents/" + id + "/download";
        this.downloadUrl = "/api/binaryContents/" + id + "/download";
    }
}