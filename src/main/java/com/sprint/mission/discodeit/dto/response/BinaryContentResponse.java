package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        String filename,
        String contentType,
        int fileSize
) {
    public static BinaryContentResponse from(BinaryContent content) {
        return new BinaryContentResponse(
                content.getId(),
                content.getFilename(),
                content.getContentType(),
                content.getFileSize()
        );
    }
}
