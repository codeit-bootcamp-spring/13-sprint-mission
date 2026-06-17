package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record BinaryContentResponse(
        UUID id,
        UUID userId,
        UUID messageId,
        String contentType,
        String fileName
) {

    public static BinaryContentResponse from(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getUserId(),
                binaryContent.getMessageId(),
                binaryContent.getContentType(),
                binaryContent.getFileName()
        );
    }
}