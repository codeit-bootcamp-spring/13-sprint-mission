package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;

public record BinaryContentResponse(
        String fileName,
        String contentType,
        int fileSize
) {
    public static BinaryContentResponse from(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getFileSize()
        );
    }
}
